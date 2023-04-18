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

data "aws_iam_policy_document" "cross_account_access" {
  statement {
    sid    = "Allow cross account access"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = var.cross_account_arns
    }

    actions = [
      "s3:GetObject",
      "s3:ListBucket",
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "cloudtrail_access" {
  statement {
    sid    = "Allow cloudtrail logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudtrail.amazonaws.com"]
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
}

locals {
  cross_account_policy = length(var.cross_account_arns) != 0 ? [data.aws_iam_policy_document.cross_account_access.json] : []
  cloudtrail_policy    = var.allow_cloudtrail_logs ? [data.aws_iam_policy_document.cloudtrail_access.json] : []
  source_policies      = concat(local.cross_account_policy, local.cloudtrail_policy)
}

data "aws_iam_policy_document" "bucket_policy" {
  source_policy_documents = local.source_policies

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
