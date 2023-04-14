# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-bucket-logging
#tfsec:ignore:aws-s3-enable-versioning
resource "aws_s3_bucket" "log_bucket" {
  bucket = "${var.environment}-${var.name}-logs-gdx-data-share-poc"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "log_bucket_acl" {
  bucket = aws_s3_bucket.log_bucket.id
  acl    = "log-delivery-write"
}

resource "aws_s3_bucket_server_side_encryption_configuration" "log_bucket" {
  bucket = aws_s3_bucket.log_bucket.bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = var.use_kms ? aws_kms_key.bucket[0].arn : null
      sse_algorithm     = var.use_kms ? "aws:kms" : "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "log_bucket" {
  bucket = aws_s3_bucket.log_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "log_bucket_lifecycle" {
  bucket = aws_s3_bucket.log_bucket.id

  rule {
    id = "${aws_s3_bucket.log_bucket.bucket}-lifecycle-rule"
    transition {
      days          = 180
      storage_class = "INTELLIGENT_TIERING"
    }
    status = "Enabled"
  }
}

data "aws_iam_policy_document" "log_bucket_deny_insecure_transport" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.log_bucket.arn,
      "${aws_s3_bucket.log_bucket.arn}/*",
    ]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values = [
        "false"
      ]
    }
  }
}

resource "aws_s3_bucket_policy" "log_bucket_deny_insecure_transport" {
  bucket = aws_s3_bucket.log_bucket.id
  policy = data.aws_iam_policy_document.log_bucket_deny_insecure_transport.json
}

resource "aws_s3_bucket_notification" "log_bucket_notification" {
  bucket = aws_s3_bucket.log_bucket.id

  topic {
    topic_arn     = var.sns_arn
    events        = ["s3:ObjectRemoved:Delete"]
    filter_prefix = "AWSLogs/"
    filter_suffix = ".log"
  }
}
