# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-bucket-logging
#tfsec:ignore:aws-s3-enable-versioning
resource "aws_s3_bucket" "cloudfront_logs_bucket" {
  bucket = "grafana-cloudfront-logs"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_public_access_block" "cloudfront_logs_bucket_public_access" {
  bucket                  = aws_s3_bucket.cloudfront_logs_bucket.id
  block_public_acls       = true
  block_public_policy     = true
  restrict_public_buckets = true
  ignore_public_acls      = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "cloudfront_logs_bucket_encryption" {
  bucket = aws_s3_bucket.cloudfront_logs_bucket.bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.log_key.arn
      sse_algorithm     = "aws:kms"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "cloudfront_logs_bucket_lifecycle_rule" {
  bucket = aws_s3_bucket.cloudfront_logs_bucket.id

  rule {
    id     = "grafana-logs"
    status = "Enabled"
    expiration {
      days = 7
    }
  }
}

resource "aws_s3_bucket_acl" "cloudfront_logs_bucket_grants" {
  bucket = aws_s3_bucket.cloudfront_logs_bucket.id
  access_control_policy {
    grant {
      grantee {
        id   = data.aws_canonical_user_id.current.id
        type = "CanonicalUser"
      }
      permission = "FULL_CONTROL"
    }

    grant {
      grantee {
        id   = "c4c1ede66af53448b93c283ce9448c4ba468c9432aa01d700d3878632f77d2d0" # The Canonical ID for Cloudfront
        type = "CanonicalUser"
      }
      permission = "FULL_CONTROL"
    }

    owner {
      id = data.aws_canonical_user_id.current.id
    }
  }
}

data "aws_iam_policy_document" "deny_insecure_transport" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.cloudfront_logs_bucket.arn,
      "${aws_s3_bucket.cloudfront_logs_bucket.arn}/*",
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
  bucket = aws_s3_bucket.cloudfront_logs_bucket.id
  policy = data.aws_iam_policy_document.deny_insecure_transport.json
}

resource "aws_s3_bucket_notification" "cloudfront_bucket_notification" {
  bucket = aws_s3_bucket.cloudfront_logs_bucket.id

  topic {
    topic_arn     = var.sns_topic_arn
    events        = ["s3:ObjectRemoved:Delete"]
    filter_prefix = "AWSLogs/"
    filter_suffix = ".log"
  }
}
