#tfsec:ignore:aws-ec2-require-vpc-flow-logs-for-all-vpcs
module "vpc" {
  source = "git::https://github.com/Softwire/terraform-vpc-aws?ref=24b5808095f54de6909ae03c9b0ccc21a735c6c3"

  name_prefix = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr    = var.vpc_cidr

  # At least two availability zones are required in order to set up a load balancer, so that the
  # infrastructure is kept consistent with other environments which use multiple availability zones.
  availability_zones = data.aws_availability_zones.available.zone_ids

  tags_default = {
    Environment = var.environment
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}
