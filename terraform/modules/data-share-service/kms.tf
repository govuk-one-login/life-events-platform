resource "aws_kms_key" "log_key" {
  description         = "Logs encryption key"
  enable_key_rotation = true
}