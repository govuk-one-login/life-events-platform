locals {
  env = "shared"
  default_tags = {
    Product     = "DI Life Events Platform"
    Environment = local.env
    Owner       = "di-life-events-platform@digital.cabinet-office.gov.uk"
    Source      = "terraform"
    Repository  = "https://github.com/alphagov/di-data-life-events-platform"
  }
  vpc_cidr            = "10.158.32.0/20"
  private_subnet_cidr = cidrsubnet(local.vpc_cidr, 1, 0)
  public_subnet_cidr  = cidrsubnet(local.vpc_cidr, 1, 1)
}

terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
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
  region              = data.aws_region.current.name
  name                = "sns"
  notification_emails = ["di-life-events-platform@digital.cabinet-office.gov.uk"]
}

module "vpc" {
  source = "../modules/vpc"

  environment = local.env
  account_id  = data.aws_caller_identity.current.account_id
  region      = data.aws_region.current.name
  name_prefix = "${local.env}-"
  vpc_cidr    = local.vpc_cidr

  sns_topic_arn = module.sns.topic_arn

  acl_ingress_private = [
    # Allow traffic from loadbalancer
    {
      rule_no    = 1
      from_port  = 3000
      to_port    = 3000
      cidr_block = local.public_subnet_cidr
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Deny port 3389 (RDP)
    {
      rule_no    = 2
      from_port  = 3389
      to_port    = 3389
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    # Allow response to outbound calls
    {
      rule_no    = 3
      from_port  = 1024
      to_port    = 65535
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    {
      rule_no    = 100
      from_port  = 0
      to_port    = 0
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = -1
    }
  ]
  acl_egress_private = [
    # HTTPS calls from ECS service to internet
    {
      rule_no    = 1
      from_port  = 443
      to_port    = 443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Allow response to inbound calls
    {
      rule_no    = 2
      from_port  = 1024
      to_port    = 65535
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    {
      rule_no    = 100
      from_port  = 0
      to_port    = 0
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = -1
    }
  ]

  acl_ingress_public = [
    # Inbound loadbalancer traffic
    {
      rule_no    = 1
      from_port  = 443
      to_port    = 443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Deny port 3389 (RDP)
    {
      rule_no    = 2
      from_port  = 3389
      to_port    = 3389
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    # Allow response to outbound calls
    {
      rule_no    = 3
      from_port  = 1024
      to_port    = 65535
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    {
      rule_no    = 100
      from_port  = 0
      to_port    = 0
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = -1
    }
  ]
  acl_egress_public = [
    # Outbound calls mainly to AWS services
    {
      rule_no    = 1
      from_port  = 443
      to_port    = 443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Allow response to inbound calls
    {
      rule_no    = 2
      from_port  = 1024
      to_port    = 65535
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    {
      rule_no    = 100
      from_port  = 0
      to_port    = 0
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = -1
    }
  ]
}

module "route53" {
  source = "../modules/route53"

  hosted_zone_name = "non-prod-grafana.share-life-events.service.gov.uk"
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

  hosted_zone_id   = module.route53.zone_id
  hosted_zone_name = module.route53.name
}

module "ecr" {
  source = "../modules/ecr"
}

module "policies" {
  providers = {
    aws           = aws
    aws.us-east-1 = aws.us-east-1
  }

  source = "../modules/policies"
}

module "iam_user_roles" {
  source = "../modules/iam_user_roles"

  terraform_lock_table_name = "gdx-data-share-poc-lock"
  account_id                = data.aws_caller_identity.current.account_id
}

module "statuscake" {
  source = "../modules/statuscake"
  env_url_pair = {
    dev  = "https://dev.share-life-events.service.gov.uk/health"
    demo = "https://demo.share-life-events.service.gov.uk/health"
  }
}

module "cloudtrail" {
  source = "../modules/cloudtrail"

  account_id                  = data.aws_caller_identity.current.account_id
  region                      = data.aws_region.current.name
  environment                 = local.env
  cloudwatch_retention_period = 30

  sns_topic_arn = module.sns.topic_arn
}
