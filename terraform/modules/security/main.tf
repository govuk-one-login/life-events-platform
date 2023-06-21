terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.0"
      configuration_aliases = [aws.us-east-1]
    }
  }
}

resource "aws_securityhub_finding_aggregator" "securityhub" {
  depends_on   = [module.securityhub_local]
  linking_mode = "ALL_REGIONS"
}

module "securityhub_local" {
  source = "../securityhub"

  account_id = var.account_id

  config_role_arn   = aws_iam_role.config.arn
  config_s3_id      = module.config_s3.id
  config_s3_kms_arn = module.config_s3.kms_arn

  rules = var.rules
}

module "securityhub_global" {
  source = "../securityhub"
  providers = {
    aws = aws.us-east-1
  }

  account_id = var.account_id

  config_role_arn   = aws_iam_role.config.arn
  config_s3_id      = module.config_s3.id
  config_s3_kms_arn = module.config_s3.kms_arn

  rules = var.global_rules
}
