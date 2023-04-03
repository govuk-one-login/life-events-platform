module "vpc" {
  source = "../vpc"

  environment = var.environment
  account_id  = data.aws_caller_identity.current.account_id
  name_prefix = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr    = var.vpc_cidr
}
