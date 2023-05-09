resource "aws_ssm_parameter" "dwp_principal" {
  name  = "DWPPrincipal"
  type  = "SecureString"
  value = "placeholder"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}
