locals {
  env = "dev"
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
    key            = "terraform-dev.tfstate"
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

module "data-share-service" {
  source = "../modules/data-share-service"
  providers = {
    aws           = aws
    aws.us-east-1 = aws.us-east-1
  }
  environment                 = local.env
  region                      = data.aws_region.current.name
  ecr_url                     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
  cloudwatch_retention_period = 30
  vpc_cidr                    = "10.158.0.0/20"
  lev_url                     = module.lev_api.service_url
  db_username                 = "ecs_dev_db"
}

module "len" {
  source                      = "../modules/len"
  environment                 = local.env
  region                      = data.aws_region.current.name
  schedule                    = "cron(* 9-18 ? * MON-FRI *)"
  cloudwatch_retention_period = 30
  gdx_url                     = module.data-share-service.gdx_url
  auth_url                    = module.data-share-service.token_auth_url
  len_client_id               = module.data-share-service.len_client_id
  len_client_secret           = module.data-share-service.len_client_secret
  lev_rds_db_username         = module.lev_api.lev_rds_db_username
  lev_rds_db_password         = module.lev_api.lev_rds_db_password
  lev_rds_db_name             = module.lev_api.lev_rds_db_name
  lev_rds_db_host             = module.lev_api.lev_rds_db_host
}

module "consumer" {
  source                      = "../modules/consumer"
  environment                 = local.env
  region                      = data.aws_region.current.name
  cloudwatch_retention_period = 30
  gdx_url                     = module.data-share-service.gdx_url
  auth_url                    = module.data-share-service.token_auth_url
  consumer_client_id          = module.data-share-service.consumer_client_id
  consumer_client_secret      = module.data-share-service.consumer_client_secret
  lev_api_url                 = "https://${module.lev_api.service_url}"
  schedule                    = "cron(0,30 9-18 ? * MON-FRI *)"
}
