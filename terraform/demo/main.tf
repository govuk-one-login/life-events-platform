locals {
  env = "demo"
  default_tags = {
    Product     = "Government Data Exchange"
    Environment = local.env
    Owner       = "gdx-dev-team@digital.cabinet-office.gov.uk"
    Source      = "terraform"
    Repository  = "https://github.com/alphagov/gdx-data-share-poc"
  }
}

terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
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

  prisoner_event_enabled = "false"
  prisoner_search_url    = ""
  hmpps_auth_url         = ""

  grafana_task_role_name = data.terraform_remote_state.shared.outputs.grafana_task_role_name

  hosted_zone_id   = module.route53.zone_id
  hosted_zone_name = module.route53.name
}

module "gro_ingestion_service" {
  source = "../modules/gro_ingestion_service"

  environment = local.env
  region      = data.aws_region.current.name
}
