locals {
  metric_period = 300
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "${var.environment}-gdx-data-share-poc-ecs-logs"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_cloudwatch_log_group" "lb_sg_update" {
  name              = "/aws/lambda/${aws_lambda_function.lb_sg_update.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

module "metrics_dashboard" {
  source           = "../cloudwatch_dashboard"
  region           = var.region
  dashboard_name   = "${var.environment}-metrics-dashboard"
  metric_namespace = "${var.environment}-gdx"
  widgets = [
    {
      title  = "Old API calls",
      period = local.metric_period,
      stat   = "Sum",
      metrics = [
        "API_CALLS.IngestedEvents.count",
        "API_CALLS.CallsToLev.count",
        "API_CALLS.ResponsesFromLev.count",
        "API_CALLS.CallsToHmrc.count",
        "API_CALLS.ResponsesFromHmrc.count",
        "API_CALLS.CallsToPoll.count",
        "API_CALLS.CallsToEnrich.count",
      ]
    },
    {
      title  = "Event API calls",
      period = local.metric_period,
      stat   = "Sum",
      metrics = [
        "API_CALLS.PublishEvent.count",
        "API_CALLS.GetEvent.count",
        "API_CALLS.GetEvents.count",
        "API_CALLS.GetEventsStatus.count",
        "API_CALLS.DeleteEvent.count",
      ]
    },
    {
      title  = "Data ingest calls",
      period = local.metric_period,
      stat   = "Sum",
      metrics = [
        "API_CALLS.PublishEvent.count",
        "API_CALLS.CallsToLev.count",
        "API_RESPONSES.ResponsesFromLev.count",
      ]
    },
  ]
}
