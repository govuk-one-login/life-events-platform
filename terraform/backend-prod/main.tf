terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket         = "gdx-data-share-tfstate"
    key            = "terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-west-2"
  default_tags {
    tags = {
      Product     = "Government Data Exchange"
      Environment = "bootstrap-prod"
      Owner       = "di-life-events-platform@digital.cabinet-office.gov.uk"
      Source      = "terraform"
      Repository  = "https://github.com/alphagov/di-data-life-events-platform"
    }
  }
}

data "aws_caller_identity" "current" {}

module "bootstrap" {
  source               = "../modules/bootstrap"
  s3_bucket_name       = "gdx-data-share-tfstate"
  dynamo_db_table_name = "gdx-data-share-lock"
}

module "github_iam" {
  source                    = "../modules/github_iam"
  account_id                = data.aws_caller_identity.current.account_id
  environments              = ["prod"]
  terraform_lock_table_name = "gdx-data-share-lock"
}
