locals {
  env = "prod"
  default_tags = {
    Product     = "DI Life Events Platform"
    Environment = local.env
    Owner       = "di-life-events-platform@digital.cabinet-office.gov.uk"
    Source      = "terraform"
    Repository  = "https://github.com/alphagov/di-data-life-events-platform"
  }
}

terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
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

provider "aws" {
  alias  = "eu-west-1"
  region = "eu-west-1"
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

data "aws_region" "current" {}

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
    },
    {
      name         = data.terraform_remote_state.shared.outputs.hosted_zone_name
      name_servers = data.terraform_remote_state.shared.outputs.hosted_zone_name_servers
    }
  ]
}

module "sns" {
  source = "../modules/sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = local.env
  region              = data.aws_region.current.name
  name                = "sns"
  notification_emails = ["di-life-events-platform@digital.cabinet-office.gov.uk"]
}

module "sns_eu_west_1" {
  providers = {
    aws = aws.eu-west-1
  }
  source = "../modules/sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = local.env
  region              = "eu-west-1"
  name                = "sns"
  notification_emails = ["di-life-events-platform@digital.cabinet-office.gov.uk"]
}

module "sns_us_east_1" {
  providers = {
    aws = aws.us-east-1
  }
  source = "../modules/sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = local.env
  region              = "us-east-1"
  name                = "sns"
  notification_emails = ["di-life-events-platform@digital.cabinet-office.gov.uk"]
}

module "assume_admin_role_alert" {
  providers = {
    aws           = aws
    aws.eu-west-1 = aws.eu-west-1
    aws.us-east-1 = aws.us-east-1
  }
  source = "../modules/assume_admin_role_alert_all_regions"

  eu_west_2_sns_arn = module.sns.topic_arn
  us_east_1_sns_arn = module.sns_us_east_1.topic_arn
  eu_west_1_sns_arn = module.sns_eu_west_1.topic_arn
}

module "iam_user_roles" {
  source = "../modules/iam_user_roles"

  terraform_lock_table_name = "gdx-data-share-lock"
  account_id                = data.aws_caller_identity.current.account_id
}
