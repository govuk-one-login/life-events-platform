locals {
  metric_namespace = "${var.environment}-gdx"
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "${var.environment}-gdx-data-share-poc-ecs-logs"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_cloudwatch_log_group" "ecs_adot_logs" {
  name              = "${var.environment}-gdx-data-share-poc-ecs-adot-logs"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_cloudwatch_log_group" "prometheus_logs" {
  name              = "${var.environment}-gdx-data-share-poc-prometheus-logs"
  retention_in_days = 3

  kms_key_id = aws_kms_key.log_key.arn
}
