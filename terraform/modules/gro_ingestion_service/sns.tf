module "sns" {
  source = "../sns"

  account_id          = data.aws_caller_identity.current.account_id
  environment         = var.environment
  name                = "gdx-gro-sns"
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
}
