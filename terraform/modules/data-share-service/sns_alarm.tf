locals {
  notification_emails = [""]
}

module "sns" {
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  name                = "gdx-alarms"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]

  arns_which_can_publish = [aws_prometheus_workspace.prometheus.arn]
}
