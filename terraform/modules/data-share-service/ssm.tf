resource "aws_ssm_parameter" "lev_api_client_name" {
  name  = "${var.environment}-lev-api-client-name"
  type  = "String"
  value = "gdx-data-share"
}

resource "aws_ssm_parameter" "lev_api_client_user" {
  name  = "${var.environment}-lev-api-client-user"
  type  = "SecureString"
  value = "gdx-data-share-user"
}

resource "aws_ssm_parameter" "prisoner_search_api_client_id" {
  name  = "${var.environment}_prisoner_search_api_client_id"
  type  = "String"
  value = "gdx-data-share-poc"
}

resource "aws_ssm_parameter" "prisoner_search_api_client_secret" {
  name  = "${var.environment}_prisoner_search_api_client_secret"
  type  = "SecureString"
  value = "secretvalue"
}

resource "aws_ssm_parameter" "prisoner_event_queue_name" {
  name  = "${var.environment}_prisoner_event_queue_name"
  type  = "String"
  value = "dps-tech-dev-gds-data-share-queue"
}

resource "aws_ssm_parameter" "prisoner_event_dlq_name" {
  name  = "${var.environment}_prisoner_event_dlq_name"
  type  = "String"
  value = "dps-tech-dev-gds-data-share-dlq"
}

resource "aws_ssm_parameter" "prisoner_event_aws_account_id" {
  name  = "${var.environment}_prisoner_event_aws_account_id"
  type  = "String"
  value = "754256621582"
}
