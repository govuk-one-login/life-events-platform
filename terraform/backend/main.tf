terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
  backend "s3" {
    bucket         = "gdx-data-share-poc-tfstate"
    key            = "terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-west-2"
  default_tags {
    tags = {
      Product     = "Government Data Exchange"
      Environment = "bootstrap"
      Owner       = "gdx-dev-team@digital.cabinet-office.gov.uk"
      Source      = "terraform"
      Repository  = "https://github.com/alphagov/gdx-data-share-poc"
    }
  }
}

data "aws_caller_identity" "current" {}

module "bootstrap" {
  source               = "../modules/bootstrap"
  s3_bucket_name       = "gdx-data-share-poc-tfstate"
  dynamo_db_table_name = "gdx-data-share-poc-lock"

  cross_account_arns = [
    "arn:aws:iam::255773200490:role/prod-github-oidc-deploy",
    "arn:aws:iam::255773200490:role/github-oidc-pull-request"
  ]
}

module "github_iam" {
  source                    = "../modules/github_iam"
  account_id                = data.aws_caller_identity.current.account_id
  environments              = ["dev", "demo", "shared"]
  terraform_lock_table_name = "gdx-data-share-poc-lock"
}
