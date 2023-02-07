#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=dcc0cee3eafc5578d93479bb7798c22a8e5baba1"

  name_prefix = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr    = var.vpc_cidr

  # At least two availability zones are required in order to set up a load balancer, so that the
  # infrastructure is kept consistent with other environments which use multiple availability zones.
  availability_zones = data.aws_availability_zones.available.zone_ids

  map_public_subnet_public_ips = true

  tags_default = {
    Environment = var.environment
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}
