resource "aws_securityhub_standards_control" "hardware_mfa" {
  for_each              = var.rules
  standards_control_arn = "arn:aws:config:eu-west-2:${var.account_id}:${each.value.rule}"
  control_status        = "DISABLED"
  disabled_reason       = each.value.disabled_reason
}
