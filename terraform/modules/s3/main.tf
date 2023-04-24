locals {
  bucket_name     = "${var.prefix}${var.prefix == "" ? "" : "-"}${var.name}${var.suffix == "" ? "" : "-"}${var.suffix}"
  log_bucket_name = "${var.prefix}${var.prefix == "" ? "" : "-"}${var.name}-logs${var.suffix == "" ? "" : "-"}${var.suffix}"
}

resource "aws_s3_bucket" "bucket" {
  bucket = local.bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "bucket_acl" {
  bucket = aws_s3_bucket.bucket.id
  acl    = "private"
}

resource "aws_s3_bucket_versioning" "bucket" {
  bucket = aws_s3_bucket.bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "bucket" {
  bucket = aws_s3_bucket.bucket.bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = var.use_kms ? aws_kms_key.bucket[0].arn : null
      sse_algorithm     = var.use_kms ? "aws:kms" : "AES256"
    }
  }
}

resource "aws_s3_bucket_logging" "bucket_logging" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.bucket.id

  target_bucket = aws_s3_bucket.log_bucket[0].id
  target_prefix = "log/"
}

resource "aws_s3_bucket_lifecycle_configuration" "bucket_lifecycle" {
  count  = var.expiration_days == null && var.tiering_noncurrent_days == null ? 0 : 1
  bucket = aws_s3_bucket.bucket.id

  dynamic "rule" {
    for_each = var.expiration_days == null ? toset([]) : toset([var.expiration_days])
    content {
      id = "${aws_s3_bucket.bucket.bucket}-lifecycle-rule"
      expiration {
        days = rule.value
      }
      status = "Enabled"
    }
  }
  dynamic "rule" {
    for_each = var.tiering_noncurrent_days == null ? toset([]) : toset([var.tiering_noncurrent_days])
    content {
      id = "${aws_s3_bucket.bucket.bucket}-lifecycle-rule"
      noncurrent_version_transition {
        noncurrent_days = rule.value
        storage_class   = "INTELLIGENT_TIERING"
      }
      status = "Enabled"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "bucket" {
  bucket = aws_s3_bucket.bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.bucket.id

  topic {
    topic_arn     = var.sns_arn
    events        = ["s3:ObjectRemoved:Delete"]
    filter_prefix = "AWSLogs/"
    filter_suffix = ".log"
  }
}
