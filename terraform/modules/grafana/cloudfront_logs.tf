# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-logging
#tfsec:ignore:aws-s3-enable-versioning
module "cloudfront_logs_bucket" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = "grafana"
  name            = "cloudfront-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket      = false
  object_writer_owner = true

  sns_arn = var.s3_event_notification_sns_topic_arn
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
