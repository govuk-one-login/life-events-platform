locals {
  ecs_task_execution_parameters = [
    aws_ssm_parameter.lev_api_client_name,
    aws_ssm_parameter.lev_api_client_user,
    aws_ssm_parameter.prisoner_search_api_client_id,
    aws_ssm_parameter.prisoner_search_api_client_secret,
    aws_ssm_parameter.prisoner_event_queue_name,
    aws_ssm_parameter.prisoner_event_dlq_name,
    aws_ssm_parameter.prisoner_event_aws_account_id,
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

resource "aws_ssm_parameter" "prisoner_search_api_client_id" {
  name  = "${var.environment}-prisoner-search-api-client-id"
  type  = "String"
  value = "gdx-data-share-poc"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

resource "aws_ssm_parameter" "prisoner_search_api_client_secret" {
  name  = "${var.environment}-prisoner-search-api-client-secret"
  type  = "SecureString"
  value = "secretvalue"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

resource "aws_ssm_parameter" "prisoner_event_queue_name" {
  name  = "${var.environment}-prisoner-event-queue-name"
  type  = "String"
  value = "dps-tech-dev-gds-data-share-queue"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

resource "aws_ssm_parameter" "prisoner_event_dlq_name" {
  name  = "${var.environment}-prisoner-event-dlq-name"
  type  = "String"
  value = "dps-tech-dev-gds-data-share-dlq"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

resource "aws_ssm_parameter" "prisoner_event_aws_account_id" {
  name  = "${var.environment}-prisoner-event-aws-account-id"
  type  = "String"
  value = "754256621582"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}
