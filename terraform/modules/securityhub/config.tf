resource "aws_config_configuration_recorder" "config" {
  name     = "config-recorder"
  role_arn = var.config_role_arn
  recording_group {
    include_global_resource_types = true
  }
}
