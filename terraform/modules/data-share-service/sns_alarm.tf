locals {
  notification_emails = []
}

resource "aws_kms_key" "sns_alarm_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sns alarm"
}

resource "aws_kms_alias" "sqs_key_alias" {
  name          = "alias/${var.environment}/sns-alarm-key"
  target_key_id = aws_kms_key.sns_alarm_key.arn
}

resource "aws_sns_topic" "sns_alarm_topic" {
  name              = "${var.environment}-gdx-alarms"
  kms_master_key_id = aws_kms_key.sns_alarm_key.arn
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  for_each  = toset(local.notification_emails)
  topic_arn = aws_sns_topic.sns_alarm_topic.arn
  protocol  = "email"
  endpoint  = each.key
}
