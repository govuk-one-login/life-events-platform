resource "aws_config_configuration_recorder" "config" {
  name     = "config-recorder"
  role_arn = var.config_role_arn
  recording_group {
    include_global_resource_types = true
  }
}

resource "aws_config_configuration_recorder_status" "config" {
  name       = aws_config_configuration_recorder.config.name
  is_enabled = true
  depends_on = [aws_config_delivery_channel.config]
}

resource "aws_config_delivery_channel" "config" {
  name           = "config-delivery-channel"
  s3_bucket_name = var.config_s3_id
  s3_kms_key_arn = var.config_s3_kms_arn
  depends_on     = [aws_config_configuration_recorder.config]
}
