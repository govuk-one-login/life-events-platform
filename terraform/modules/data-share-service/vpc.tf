module "vpc_new" {
  source = "../vpc"

  environment = var.environment
  account_id  = data.aws_caller_identity.current.account_id
  name_prefix = "${var.environment}-gdx-data-share-poc-"
  vpc_cidr    = var.vpc_cidr
}

moved {
  from = module.vpc
  to   = module.vpc_new.module.vpc
}

moved {
  from = module.flow_logs_s3
  to   = module.vpc_new.module.flow_logs_s3
}

moved {
  from = aws_flow_log.flow_logs
  to   = module.vpc_new.aws_flow_log.flow_logs
}

moved {
  from = aws_default_security_group.default_security_group
  to   = module.vpc_new.aws_default_security_group.default_security_group
}

moved {
  from = aws_default_network_acl.default_network_acl
  to   = module.vpc_new.aws_default_network_acl.default_network_acl
}
