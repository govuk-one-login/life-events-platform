data "aws_caller_identity" "current" {}

# We do not want versioning to make sure data is deleted regularly as intended
#tfsec:ignore:aws-s3-enable-versioning
module "gro_bucket" {
  source = "../s3"

  account_id              = data.aws_caller_identity.current.account_id
  region                  = var.region
  prefix                  = var.environment
  name                    = "data"
  suffix                  = "gdx-gro-poc"
  expiration_days         = 7
  notify_bucket_deletions = false
  allow_versioning        = false

  sns_arn = module.sns.topic_arn

  depends_on = [module.sns]
}
