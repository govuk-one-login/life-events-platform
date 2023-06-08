#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=9e9accca08cfa417b265f5c7d9a0c169eb36c57c"

  name_prefix          = var.name_prefix
  vpc_cidr             = var.vpc_cidr
  enable_dns_hostnames = true
  acl_ingress_private  = var.acl_ingress_private
  acl_egress_private   = var.acl_egress_private
  acl_ingress_public   = var.acl_ingress_public
  acl_egress_public    = var.acl_egress_public

  # At least two availability zones are required in order to set up a load balancer, so that the
  # infrastructure is kept consistent with other environments which use multiple availability zones.
  availability_zones = data.aws_availability_zones.available.zone_ids
}

resource "aws_default_security_group" "default_security_group" {
  vpc_id = module.vpc.vpc_id
}

resource "aws_default_network_acl" "default_network_acl" {
  default_network_acl_id = module.vpc.default_network_acl_id
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

  account_id      = var.account_id
  region          = var.region
  prefix          = var.environment
  name            = "vpc-flow-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  allow_delivery_logs = true

  sns_arn = var.sns_topic_arn
}
