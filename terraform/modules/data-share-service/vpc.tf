#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=9b31ce5bd484d4070c966be234f1facc6e380999"

  name_prefix          = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr             = var.vpc_cidr
  enable_dns_hostnames = true

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
