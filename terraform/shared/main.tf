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

module "vpc" {
  source = "../modules/vpc"

  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id
  region      = data.aws_region.current.name
  name_prefix = "${local.env}-"
  vpc_cidr    = "10.158.32.0/20"
}

module "grafana" {
  source = "../modules/grafana"
  providers = {
    aws           = aws
    aws.us-east-1 = aws.us-east-1
  }

  region     = data.aws_region.current.name
  account_id = data.aws_caller_identity.current.account_id

  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  vpc_cidr           = "10.158.32.0/20"

  ecr_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com"
}

module "securityhub" {
  source = "../modules/security_hub"

  region      = data.aws_region.current.name
  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id
  rules = [
    {
      rule            = "aws-foundational-security-best-practices/v/1.0.0/IAM.6"
      disabled_reason = "For this GDS created account this is not possible to enforce"
    },
    {
      rule            = "cis-aws-foundations-benchmark/v/1.4.0/1.6"
      disabled_reason = "For this GDS created account this is not possible to enforce"
    },
    {
      rule            = "aws-foundational-security-best-practices/v/1.0.0/ECS.5"
      disabled_reason = "Our ECS containers need write access to the root filesystem."
    },
    {
      rule            = "aws-foundational-security-best-practices/v/1.0.0/ECS.10"
      disabled_reason = "Our ECS containers run on the latest fargate versions, as shown in the ci appspec template, however Security Hub is not picking this up."
    },
    {
      rule            = "aws-foundational-security-best-practices/v/1.0.0/ECR.2"
      disabled_reason = "For grafana our tags needs to be mutable so that our latest and deployed version tracks the most recent, lev-api and aws-xray-daemon are both pull through repos so we cannot enforce immutable tags."
    },
  ]
}

module "ecr" {
  source = "../modules/ecr"
}

module "policies" {
  source = "../modules/policies"
}

moved {
  from = module.s3_policy.aws_s3_account_public_access_block.block_public_access
  to   = module.policies.aws_s3_account_public_access_block.block_public_access
}

moved {
  from = module.iam_policy.aws_iam_group.mfa_enforced
  to   = module.policies.aws_iam_group.mfa_enforced
}

moved {
  from = module.iam_policy.aws_iam_policy_document.enforce_mfa
  to   = module.policies.aws_iam_policy_document.enforce_mfa
}

moved {
  from = module.iam_policy.aws_iam_policy.enforce_mfa
  to   = module.policies.aws_iam_policy.enforce_mfa
}

moved {
  from = module.iam_policy.aws_iam_group_policy_attachment.enforce_mfa
  to   = module.policies.aws_iam_group_policy_attachment.enforce_mfa
}

locals {
  gdx_dev_team = [
    "carly.gilson",
    "ethan.mills",
    "oliver.levett",
    "oskar.williams"
  ]
}

module "iam_user_roles" {
  source = "../modules/iam_user_roles"

  admin_users     = local.gdx_dev_team
  read_only_users = local.gdx_dev_team
}
