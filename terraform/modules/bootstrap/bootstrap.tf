# Disable S3 bucket logging as this is the only bucket, revisit when there's more underlying infra/monitoring set up
#tfsec:ignore:aws-s3-enable-bucket-logging
resource "aws_s3_bucket" "state_bucket" {
  bucket = var.s3_bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "state_bucket" {
  bucket = aws_s3_bucket.state_bucket.id
  acl    = "private"
}

resource "aws_s3_bucket_versioning" "state_bucket" {
  bucket = aws_s3_bucket.state_bucket.id
  versioning_configuration {
    status     = "Enabled"
    mfa_delete = "Enabled"
  }
}

resource "aws_kms_key" "state_bucket" {
  enable_key_rotation = true
  description         = "Key used to encrypt state bucket"
}

resource "aws_kms_alias" "state_bucket_key_alias" {
  name          = "alias/state-bucket-key"
  target_key_id = aws_kms_key.state_bucket.arn
}

resource "aws_s3_bucket_server_side_encryption_configuration" "state_bucket" {
  bucket = aws_s3_bucket.state_bucket.bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.state_bucket.arn
      sse_algorithm     = "aws:kms"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "state_bucket" {
  bucket = aws_s3_bucket.state_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
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
