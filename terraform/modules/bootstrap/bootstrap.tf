module "state_bucket" {
  source = "../s3"

  account_id = data.aws_caller_identity.current.account_id
  region     = data.aws_region.current.name
  name       = var.s3_bucket_name

  tiering_noncurrent_days = 10

  sns_arn = module.sns.topic_arn

  cross_account_arns = [
    "arn:aws:iam::255773200490:role/prod-github-oidc-deploy",
    "arn:aws:iam::255773200490:role/github-oidc-pull-request"
  ]

  depends_on = [module.sns]
}

module "sns" {
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = "bootstrap"
  name                = "sns"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
}

moved {
  from = aws_kms_key.state_bucket
  to   = module.state_bucket.aws_kms_key.bucket[0]
}

moved {
  from = aws_kms_alias.state_bucket_key_alias
  to   = module.state_bucket.aws_kms_alias.bucket_alias[0]
}

moved {
  from = aws_s3_bucket.state_bucket
  to   = module.state_bucket.aws_s3_bucket.bucket
}

moved {
  from = aws_s3_bucket.log_bucket
  to   = module.state_bucket.aws_s3_bucket.log_bucket[0]
}

moved {
  from = aws_s3_bucket_acl.state_bucket
  to   = module.state_bucket.aws_s3_bucket_acl.bucket_acl
}

moved {
  from = aws_s3_bucket_acl.log_bucket_acl
  to   = module.state_bucket.aws_s3_bucket_acl.log_bucket_acl[0]
}

moved {
  from = aws_s3_bucket_versioning.state_bucket
  to   = module.state_bucket.aws_s3_bucket_versioning.bucket
}

moved {
  from = aws_s3_bucket_lifecycle_configuration.bucket_lifecycle
  to   = module.state_bucket.aws_s3_bucket_lifecycle_configuration.bucket_lifecycle[0]
}

moved {
  from = aws_s3_bucket_lifecycle_configuration.log_bucket_lifecycle
  to   = module.state_bucket.aws_s3_bucket_lifecycle_configuration.log_bucket_lifecycle[0]
}

moved {
  from = aws_s3_bucket_server_side_encryption_configuration.state_bucket
  to   = module.state_bucket.aws_s3_bucket_server_side_encryption_configuration.bucket
}

moved {
  from = aws_s3_bucket_server_side_encryption_configuration.log_bucket
  to   = module.state_bucket.aws_s3_bucket_server_side_encryption_configuration.log_bucket[0]
}

moved {
  from = aws_s3_bucket_logging.bucket_logging
  to   = module.state_bucket.aws_s3_bucket_logging.bucket_logging[0]
}

moved {
  from = aws_s3_bucket_public_access_block.state_bucket
  to   = module.state_bucket.aws_s3_bucket_public_access_block.bucket
}

moved {
  from = aws_s3_bucket_public_access_block.log_bucket
  to   = module.state_bucket.aws_s3_bucket_public_access_block.log_bucket[0]
}

moved {
  from = aws_s3_bucket_policy.bucket_policy
  to   = module.state_bucket.aws_s3_bucket_policy.deny_insecure_transport
}

moved {
  from = aws_s3_bucket_policy.log_bucket_deny_insecure_transport
  to   = module.state_bucket.aws_s3_bucket_policy.log_bucket_deny_insecure_transport[0]
}

# we're not storing anything sensitive in DynamoDB, just using it for locking, so encryption is unecessary
#tfsec:ignore:aws-dynamodb-enable-at-rest-encryption tfsec:ignore:aws-dynamodb-table-customer-key tfsec:ignore:aws-dynamodb-enable-recovery
resource "aws_dynamodb_table" "tf_lock_state" {
  name = var.dynamo_db_table_name

  billing_mode = "PAY_PER_REQUEST"

  hash_key = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }
}
