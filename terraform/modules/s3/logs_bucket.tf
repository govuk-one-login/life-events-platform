# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-logging
#tfsec:ignore:aws-s3-enable-versioning
resource "aws_s3_bucket" "log_bucket" {
  count = var.add_log_bucket ? 1 : 0

  bucket = local.log_bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "log_bucket_acl" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id
  acl    = "log-delivery-write"

  depends_on = [aws_s3_bucket_ownership_controls.log_bucket[0]]
}

resource "aws_s3_bucket_server_side_encryption_configuration" "log_bucket" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = var.use_kms ? aws_kms_key.bucket[0].arn : null
      sse_algorithm     = var.use_kms ? "aws:kms" : "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "log_bucket" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "log_bucket_lifecycle" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id

  rule {
    id = "${aws_s3_bucket.log_bucket[0].bucket}-lifecycle-rule"
    transition {
      days          = 180
      storage_class = "INTELLIGENT_TIERING"
    }
    status = "Enabled"
  }
}

data "aws_iam_policy_document" "log_bucket_deny_insecure_transport" {
  count = var.add_log_bucket ? 1 : 0

  statement {
    sid    = "Allow S3 logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
    actions = [
      "s3:PutObject",
    ]

    resources = [
      aws_s3_bucket.log_bucket[0].arn,
      "${aws_s3_bucket.log_bucket[0].arn}/*",
    ]
  }

  statement {
    sid    = "Deny Insecure Transport"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.log_bucket[0].arn,
      "${aws_s3_bucket.log_bucket[0].arn}/*",
    ]
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
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id
  policy = data.aws_iam_policy_document.log_bucket_deny_insecure_transport[0].json
}

resource "aws_s3_bucket_notification" "log_bucket_notification" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id

  topic {
    topic_arn     = var.sns_arn
    events        = ["s3:ObjectRemoved:Delete"]
    filter_prefix = "AWSLogs/"
    filter_suffix = ".log"
  }
}

resource "aws_s3_bucket_ownership_controls" "log_bucket" {
  count = var.add_log_bucket ? 1 : 0

  bucket = aws_s3_bucket.log_bucket[0].id

  rule {
    object_ownership = "ObjectWriter"
  }
}
