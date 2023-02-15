locals {
  notification_emails = []
}

resource "aws_sns_topic" "sns_alarm_topic" {
  name = "${var.environment}-gdx-alarms"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  for_each  = toset(local.notification_emails)
  topic_arn = aws_sns_topic.sns_alarm_topic.arn
  protocol  = "email"
  endpoint  = each.key
}
