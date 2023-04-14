
resource "aws_ssm_parameter" "statuscake_api_key" {
  name  = "statuscake-api-key"
  type  = "SecureString"
  value = "secretvalue"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}
