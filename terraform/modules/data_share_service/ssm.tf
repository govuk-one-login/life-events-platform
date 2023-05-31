locals {
  ecs_task_execution_parameters = [
    aws_ssm_parameter.lev_api_client_name,
    aws_ssm_parameter.lev_api_client_user,
  ]
}

resource "aws_ssm_parameter" "lev_api_client_name" {
  name  = "${var.environment}-lev-api-client-name"
  type  = "String"
  value = "gdx-data-share"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

resource "aws_ssm_parameter" "lev_api_client_user" {
  name  = "${var.environment}-lev-api-client-user"
  type  = "SecureString"
  value = "gdx-data-share-user"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}
