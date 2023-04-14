# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-bucket-logging
#tfsec:ignore:aws-s3-enable-versioning
module "cloudfront_logs_bucket" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  environment     = grafana
  name            = "cloudfront-logs"
  expiration_days = 180

  add_log_bucket = false

  sns_arn = var.s3_event_notification_sns_topic_arn
}

moved {
  from = aws_s3_bucket.cloudfront_logs_bucket
  to   = module.cloudfront_logs_bucket.aws_s3_bucket.bucket
}

moved {
  from = aws_s3_bucket_public_access_block.cloudfront_logs_bucket_public_access
  to   = module.cloudfront_logs_bucket.aws_s3_bucket_public_access_block.bucket
}

moved {
  from = aws_s3_bucket_server_side_encryption_configuration.cloudfront_logs_bucket_encryption
  to   = module.cloudfront_logs_bucket.aws_s3_bucket_server_side_encryption_configuration.bucket
}

moved {
  from = aws_s3_bucket_lifecycle_configuration.cloudfront_logs_bucket_lifecycle_rule
  to   = module.cloudfront_logs_bucket.aws_s3_bucket_lifecycle_configuration.bucket_lifecycle
}

moved {
  from = aws_s3_bucket_policy.deny_insecure_transport
  to   = module.cloudfront_logs_bucket.aws_s3_bucket_policy.deny_insecure_transport
}

moved {
  from = aws_s3_bucket_notification.cloudfront_bucket_notification
  to   = module.cloudfront_logs_bucket.aws_s3_bucket_notification.bucket_notification
}

resource "aws_s3_bucket_acl" "cloudfront_logs_bucket_grants" {
  bucket = module.cloudfront_logs_bucket.id
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
