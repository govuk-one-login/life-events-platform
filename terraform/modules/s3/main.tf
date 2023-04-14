resource "aws_s3_bucket" "bucket" {
  bucket = "${var.environment}-${var.name}-gdx-data-share-poc"

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
  count  = var.expiration_days == null ? 0 : 1
  bucket = aws_s3_bucket.bucket.id

  rule {
    id = "${aws_s3_bucket.bucket.bucket}-lifecycle-rule"
    expiration {
      days = var.expiration_days
    }
    status = "Enabled"
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

data "aws_iam_policy_document" "bucket_policy" {
  statement {
    sid    = "Allow delivery logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["delivery.logs.amazonaws.com"]
    }
    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }

  statement {
    sid    = "Allow LB logs"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::652711504416:root"]
    }
    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }

  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
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

resource "aws_s3_bucket_policy" "deny_insecure_transport" {
  bucket = aws_s3_bucket.bucket.id
  policy = data.aws_iam_policy_document.bucket_policy.json
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.bucket.id

  topic {
    topic_arn     = var.sns_arn
    events        = ["s3:ObjectRemoved:Delete"]
    filter_prefix = "AWSLogs/"
    filter_suffix = ".log"
  }
}
