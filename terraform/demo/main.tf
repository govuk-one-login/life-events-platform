terraform {
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
      environment = "demo"
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
      environment = "demo"
    }
  }
}

data "aws_caller_identity" "current" {}

module "ecr" {
  source = "../modules/ecr"
}

module "ecs" {
  source = "../modules/ecs"
  environment = "poc"
  ecr_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com/ecr-repo"
}

module "lev_api" {
  source = "../modules/lev_api"
  providers = {
    aws = aws.eu-west-1
  }
  environment_name = "demo"
}
