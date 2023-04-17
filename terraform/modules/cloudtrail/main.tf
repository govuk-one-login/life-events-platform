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

  cloud_watch_logs_group_arn = "${aws_cloudwatch_log_group.cloudtrail.arn}:*"
  cloud_watch_logs_role_arn  = aws_iam_role.cloudtrail_role.arn

  kms_key_id = aws_kms_key.cloudtrail.arn

  enable_log_file_validation = true

  is_multi_region_trail = true
}

data "aws_iam_policy_document" "cloudtrail_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["cloudtrail.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "cloudtrail_role" {
  name               = "${var.environment}-cloudtrail"
  assume_role_policy = data.aws_iam_policy_document.cloudtrail_assume_role.json
}
