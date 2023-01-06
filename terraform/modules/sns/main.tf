data "aws_caller_identity" "current" {}

resource "aws_sns_topic" "topic" {
  name_prefix       = "${var.environment}-gdx-sns-topic"
  display_name      = var.topic_display_name
  kms_master_key_id = aws_kms_key.sns_key.arn
}

resource "aws_kms_key" "sns_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sns queue"
}

resource "aws_kms_alias" "sns_key_alias" {
  name          = "alias/${var.environment}/sns-key"
  target_key_id = aws_kms_key.sns_key.arn
}
