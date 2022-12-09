terraform {
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
      environment = "dev"
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
      environment = "dev"
    }
  }
}

data "aws_caller_identity" "current" {}

module "lev_api" {
  source = "../modules/lev_api"
  providers = {
    aws = aws.eu-west-1
  }
  environment_name = "dev"
}

module "ecs" {
  source = "../modules/ecs"
  environment = "dev"
  ecr_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com/ecr-repo"
}