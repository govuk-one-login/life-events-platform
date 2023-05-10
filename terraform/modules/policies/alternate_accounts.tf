resource "aws_account_alternate_contact" "security" {
  alternate_contact_type = "SECURITY"

  name          = "GDX Dev Team"
  title         = "Dev Team"
  email_address = "gdx-dev-team@digital.cabinet-office.gov.uk"
  phone_number  = data.aws_ssm_parameter.security_contact_number.value
}

resource "aws_ssm_parameter" "security_contact_number" {
  name  = "security-contact-number"
  type  = "SecureString"
  value = "+123456789"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

data "aws_ssm_parameter" "security_contact_number" {
  name = "security-contact-number"

  depends_on = [aws_ssm_parameter.security_contact_number]
}
