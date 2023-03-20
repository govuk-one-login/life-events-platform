locals {
  env = "shared"
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
    key            = "terraform-shared.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "gdx-data-share-poc-lock"
    encrypt        = true
  }
}

provider "aws" {
  alias  = "eu-west-2"
  region = "eu-west-2"
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

data "aws_caller_identity" "current" {}

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_region" "current" {}

#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=24b5808095f54de6909ae03c9b0ccc21a735c6c3"

  name_prefix = "shared-"
  vpc_cidr    = "10.158.32.0/20"

  # At least two availability zones are required in order to set up a load balancer, so that the
  # infrastructure is kept consistent with other environments which use multiple availability zones.
  availability_zones = data.aws_availability_zones.available.zone_ids

  enable_dns_hostnames = "true"

  tags_default = {
    Environment = local.env
  }
}

module "grafana" {
  source = "./modules/grafana"
  providers = {
    aws           = aws
    aws.us-east-1 = aws.us-east-1
  }

  region     = "eu-west-2"
  account_id = data.aws_caller_identity.current.account_id

  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  vpc_cidr           = "10.158.32.0/20"
}

module "github_env_iam" {
  source      = "../modules/github_env_iam"
  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id
}

module "github_pr_iam" {
  source     = "./modules/github_pr_iam"
  account_id = data.aws_caller_identity.current.account_id
}

module "securityhub" {
  source     = "./modules/security_hub"
  region     = data.aws_region.current.name
  account_id = data.aws_caller_identity.current.account_id
  rules      = var.security_rules
}

module "ecr" {
  source = "./modules/ecr"
}

module "s3_policy" {
  source = "./modules/s3_policy"
}
