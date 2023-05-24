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
  region              = data.aws_region.current.name
  name                = "sns"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
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
