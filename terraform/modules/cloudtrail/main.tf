module "bucket" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = var.environment
  name            = "cloudtrail-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  allow_cloudtrail_logs = true

  sns_arn = var.s3_event_notification_sns_topic_arn
}

resource "aws_cloudtrail" "cloudtrail" {
  name                          = "${var.environment}-cloudtrail"
  s3_bucket_name                = module.bucket.id
  include_global_service_events = true
}
