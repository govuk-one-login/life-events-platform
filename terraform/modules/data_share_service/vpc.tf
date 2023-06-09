locals {
  private_subnet_cidr = cidrsubnet(var.vpc_cidr, 1, 0)
  public_subnet_cidr  = cidrsubnet(var.vpc_cidr, 1, 1)

  private_acl_ingress_rules = [
    # Allow traffic from loadbalancer
    {
      rule_no    = 1
      from_port  = 8080
      to_port    = 8080
      cidr_block = local.public_subnet_cidr
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Allow traffic to database from ECS service across private subnets
    {
      rule_no    = 2
      from_port  = 45678
      to_port    = 45678
      cidr_block = local.private_subnet_cidr
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Deny port 3389 (RDP)
    {
      rule_no    = 3
      from_port  = 3389
      to_port    = 3389
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    # Allow response to outbound calls
    {
      rule_no    = 4
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
  private_acl_egress_rules = [
    # Allow traffic to database from ECS service
    {
      rule_no    = 1
      from_port  = 45678
      to_port    = 45678
      cidr_block = local.private_subnet_cidr
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # HTTPS calls from ECS service to internet
    {
      rule_no    = 2
      from_port  = 443
      to_port    = 443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Allow response to inbound calls
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

  public_acl_ingress_rules = [
    # Inbound service traffic
    {
      rule_no    = 1
      from_port  = 443
      to_port    = 443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Inbound service test traffic
    {
      rule_no    = 2
      from_port  = 8443
      to_port    = 8443
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = "tcp"
    },
    # Deny port 3389 (RDP)
    {
      rule_no    = 3
      from_port  = 3389
      to_port    = 3389
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    # Allow response to outbound calls
    {
      rule_no    = 4
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
  public_acl_egress_rules = [
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

module "vpc" {
  source = "../vpc"

  environment   = var.environment
  account_id    = data.aws_caller_identity.current.account_id
  region        = var.region
  name_prefix   = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr      = var.vpc_cidr
  sns_topic_arn = module.sns.topic_arn

  acl_egress_private  = local.private_acl_egress_rules
  acl_ingress_private = local.private_acl_ingress_rules
  acl_ingress_public  = local.public_acl_ingress_rules
  acl_egress_public   = local.public_acl_egress_rules
}
