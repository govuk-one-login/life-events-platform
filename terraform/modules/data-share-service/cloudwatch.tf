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

module "internal_alarm_dashboard" {
  source           = "../cloudwatch_alarm_dashboard"
  region           = var.region
  dashboard_name   = "${var.environment}-queue-alarm-dashboard"
  metric_namespace = local.metric_namespace
  widgets = [
    {
      title  = aws_cloudwatch_metric_alarm.queue_process_error_rate.alarm_description,
      alarms = [aws_cloudwatch_metric_alarm.queue_process_error_rate.arn]
    },
    {
      title  = aws_cloudwatch_metric_alarm.queue_process_error_number.alarm_description,
      alarms = [aws_cloudwatch_metric_alarm.queue_process_error_number.arn]
    },
  ]
}
