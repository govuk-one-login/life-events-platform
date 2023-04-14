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
    statuscake = {
      source  = "StatusCakeDev/statuscake"
      version = ">= 2.1.0"
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

module "sns" {
  source = "../modules/sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = local.env
  name                = "sns"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]

  allow_s3_notification = true
}

module "vpc" {
  source = "../modules/vpc"

  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id
  region      = data.aws_region.current.name
  name_prefix = "${local.env}-"
  vpc_cidr    = "10.158.32.0/20"

  sns_topic_arn = module.sns.topic_arn

  depends_on = [module.sns]
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

  s3_event_notification_sns_topic_arn = module.sns.topic_arn

  depends_on = [module.sns]
}

module "securityhub" {
  source = "../modules/security_hub"

  region      = data.aws_region.current.name
  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id

  s3_event_notification_sns_topic_arn = module.sns.topic_arn

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
    {
      rule            = "aws-foundational-security-best-practices/v/1.0.0/EC2.10"
      disabled_reason = "GPC-315: We only use EC2 as bastions for access to our RDS, so we do not need to configure VPC endpoints for EC2."
    },
  ]

  depends_on = [module.sns]
}

module "ecr" {
  source = "../modules/ecr"
}

module "policies" {
  source = "../modules/policies"
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

#module "statuscake" {
#  source      = "../modules/statuscake"
#  env_url_pair =  {
#    dev = "https://dev.share-life-events.service.gov.uk/health/ping"
#    demo = "https://demo.share-life-events.service.gov.uk/health/ping"
#  }
#}
