data "aws_canonical_user_id" "current" {}

resource "aws_s3_bucket" "cloudfront_logs_bucket" {
  bucket = "${var.environment}-cloudfront-logs"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "cloudfront_logs_bucket_lifecycle_rule" {
  bucket = aws_s3_bucket.cloudfront_logs_bucket.id

  rule {
    id     = "${var.environment}-logs"
    status = "Enabled"
    expiration {
      days = 180
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