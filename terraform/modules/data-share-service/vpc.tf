#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=9e9accca08cfa417b265f5c7d9a0c169eb36c57c"

  name_prefix          = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr             = var.vpc_cidr
  enable_dns_hostnames = true
  acl_ingress_private = [
    {
      rule_no    = 1
      from_port  = 22
      to_port    = 22
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    {
      rule_no    = 2
      from_port  = 3389
      to_port    = 3389
      cidr_block = "0.0.0.0/0"
      action     = "DENY"
      protocol   = "tcp"
    },
    {
      rule_no    = 100
      from_port  = 0
      to_port    = 0
      cidr_block = "0.0.0.0/0"
      action     = "ALLOW"
      protocol   = -1
    }
  ]

  # At least two availability zones are required in order to set up a load balancer, so that the
  # infrastructure is kept consistent with other environments which use multiple availability zones.
  availability_zones = data.aws_availability_zones.available.zone_ids
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_flow_log" "flow_logs" {
  traffic_type = "ALL"
  vpc_id       = module.vpc.vpc_id

  log_destination_type = "s3"
  log_destination      = module.flow_logs_s3.arn
}

module "flow_logs_s3" {
  source = "../s3"

  environment = var.environment
  name        = "vpc-flow-logs"
}
