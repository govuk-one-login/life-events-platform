locals {
  env = "demo"
  default_tags = {
    Product     = "Government Data Exchange"
    Environment = local.env
    Owner       = "di-life-events-platform@digital.cabinet-office.gov.uk"
    Source      = "terraform"
    Repository  = "https://github.com/alphagov/di-data-life-events-platform"
  }
}

terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket         = "gdx-data-share-poc-tfstate"
    key            = "terraform-demo.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-west-2"
  default_tags {
    tags = local.default_tags
  }
}

provider "aws" {
  region = "eu-west-1"
  alias  = "eu-west-1"
  default_tags {
    tags = local.default_tags
  }
}

provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"
  default_tags {
    tags = local.default_tags
  }
}

data "terraform_remote_state" "shared" {
  backend = "s3"

  config = {
    bucket         = "gdx-data-share-poc-tfstate"
    key            = "terraform-shared.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

module "lev_api" {
  source = "../modules/lev_api"
  providers = {
    aws = aws.eu-west-1
  }
  environment = local.env
  ecr_url     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
}

module "route53" {
  source = "../modules/route53"

  hosted_zone_name = "demo.share-life-events.service.gov.uk"
}

module "data_share_service" {
  source = "../modules/data_share_service"
  providers = {
    aws.us-east-1 = aws.us-east-1
    aws.eu-west-1 = aws.eu-west-1
  }
  environment                 = local.env
  region                      = data.aws_region.current.name
  ecr_url                     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
  cloudwatch_retention_period = 365
  vpc_cidr                    = "10.158.16.0/20"
  lev_url                     = module.lev_api.service_url
  db_username                 = "ecs_demo_db"

  grafana_task_role_name = data.terraform_remote_state.shared.outputs.grafana_task_role_name

  hosted_zone_id   = module.route53.zone_id
  hosted_zone_name = module.route53.name

  delete_event_function_arn  = module.gro_ingestion_service.delete_event_function_arn
  delete_event_function_name = module.gro_ingestion_service.delete_event_function_name
  enrich_event_function_arn  = module.gro_ingestion_service.enrich_event_function_arn
  enrich_event_function_name = module.gro_ingestion_service.enrich_event_function_name

  admin_alerts_enabled           = false
  database_tunnel_alerts_enabled = false

  admin_login_allowed_ip_blocks = [
    # GDS https://sites.google.com/a/digital.cabinet-office.gov.uk/gds/working-at-gds/gds-internal-it/gds-internal-it-network-public-ip-addresses
    "213.86.153.212/32",
    "213.86.153.213/32",
    "213.86.153.214/32",
    "213.86.153.235/32",
    "213.86.153.236/32",
    "213.86.153.237/32",
    "213.86.153.211/32",
    "213.86.153.231/32",
    "51.149.8.0/25",
    "51.149.8.128/29",
    # SW
    "31.221.86.178/32",
    "167.98.33.82/32",
    "82.163.115.98/32",
    "87.224.105.250/32"
  ]
}

module "gro_ingestion_service" {
  source = "../modules/gro_ingestion_service"

  environment = local.env
  region      = data.aws_region.current.name
  account_id  = data.aws_caller_identity.current.account_id

  gdx_url                 = module.data_share_service.gdx_url
  auth_url                = module.data_share_service.token_auth_url
  publisher_client_id     = module.data_share_service.gro_ingestion_client_id
  publisher_client_secret = module.data_share_service.gro_ingestion_client_secret

  insert_xml_lambda_schedule = "rate(1 hour)"
}
