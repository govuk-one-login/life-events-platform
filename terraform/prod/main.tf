locals {
  env = "prod"
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
    bucket         = "gdx-data-share-tfstate"
    key            = "terraform-prod.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-west-2"
  default_tags {
    tags = local.default_tags
  }
}

data "terraform_remote_state" "dev" {
  backend = "s3"

  config = {
    bucket         = "gdx-data-share-poc-tfstate"
    key            = "terraform-dev.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

data "terraform_remote_state" "demo" {
  backend = "s3"

  config = {
    bucket         = "gdx-data-share-poc-tfstate"
    key            = "terraform-demo.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

module "route53" {
  source = "../modules/route53"

  hosted_zone_name = "share-life-events.service.gov.uk"

  subdomains = [
    {
      name         = data.terraform_remote_state.dev.outputs.hosted_zone_name
      name_servers = data.terraform_remote_state.dev.outputs.hosted_zone_name_servers
    },
    {
      name         = data.terraform_remote_state.demo.outputs.hosted_zone_name
      name_servers = data.terraform_remote_state.demo.outputs.hosted_zone_name_servers
    }
  ]
}
