locals {
  env = "demo"
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
    tags = {
      source      = "terraform"
      repository  = "https://github.com/alphagov/gdx-data-share-poc"
      environment = local.env
    }
  }
}

provider "aws" {
  region = "eu-west-1"
  alias  = "eu-west-1"
  default_tags {
    tags = {
      source      = "terraform"
      repository  = "https://github.com/alphagov/gdx-data-share-poc"
      environment = local.env
    }
  }
}

provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"
  default_tags {
    tags = {
      source      = "terraform"
      repository  = "https://github.com/alphagov/gdx-data-share-poc"
      environment = local.env
    }
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

module "ecr" {
  source = "../modules/ecr"
}

module "lev_api" {
  source = "../modules/lev_api"
  providers = {
    aws = aws.eu-west-1
  }
  environment = local.env
  ecr_url     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
}

module "data-share-service" {
  source = "../modules/data-share-service"
  providers = {
    aws.us-east-1 = aws.us-east-1
    aws.eu-west-1 = aws.eu-west-1
  }
  environment                 = local.env
  region                      = data.aws_region.current.name
  ecr_url                     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
  cloudwatch_retention_period = 30
  vpc_cidr                    = "10.158.16.0/20"
  lev_url                     = module.lev_api.service_url
  db_username                 = "ecs_demo_db"
  externally_allowed_cidrs = [
    "82.163.115.96/27", "87.224.105.240/29", "87.224.105.248/29", "31.221.86.176/28", "167.98.33.80/28", # Softwire
  ]

  prisoner_event_enabled = "false"
  prisoner_search_url    = ""
  hmpps_auth_url         = ""

  grafana_task_role_name      = data.terraform_remote_state.shared.outputs.grafana_task_role_name
  top_level_route53_zone_id   = data.terraform_remote_state.shared.outputs.route53_zone_id
  top_level_route53_zone_name = data.terraform_remote_state.shared.outputs.route53_zone_name
}
