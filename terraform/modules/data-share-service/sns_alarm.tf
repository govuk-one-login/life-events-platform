locals {
  notification_emails = [""]
}

module "sns" {
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  name                = "gdx-alarms"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]

  prometheus_arn = aws_prometheus_workspace.prometheus.arn
}

moved {
  from = aws_kms_key.sns_alarm_key
  to   = module.sns.aws_kms_key.sns_key
}

moved {
  from = aws_kms_alias.sqs_key_alias
  to   = module.sns.aws_kms_alias.sns_key_alias
}

moved {
  from = aws_sns_topic.sns_alarm_topic
  to   = module.sns.aws_sns_topic.sns_topic
}

moved {
  from = aws_sns_topic_policy.sns_access
  to   = module.sns.aws_sns_topic_policy.sns_access
}

moved {
  from = aws_sns_topic_subscription.user_updates_sqs_target
  to   = module.sns.aws_sns_topic_subscription.notification_emails
}
