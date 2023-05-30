module "sns" {
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  region              = var.region
  name                = "gdx-alarms"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]

  arns_which_can_publish = [aws_prometheus_workspace.prometheus.arn]
}

module "sns-us-east-1" {
  providers = {
    aws = aws.us-east-1
  }
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  region              = "us-east-1"
  name                = "gdx-alarms"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
}

module "sns-eu-west-1" {
  providers = {
    aws = aws.eu-west-1
  }
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  region              = "eu-west-1"
  name                = "gdx-alarms"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
}
