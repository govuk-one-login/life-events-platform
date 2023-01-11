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
        { name = "API_CALLS.IngestedEvents.count" },
        { name = "API_CALLS.CallsToLev.count" },
        { name = "API_CALLS.ResponsesFromLev.count" },
        { name = "API_CALLS.CallsToHmrc.count" },
        { name = "API_CALLS.ResponsesFromHmrc.count" },
        { name = "API_CALLS.CallsToPoll.count" },
        { name = "API_CALLS.CallsToEnrich.count" },
      ]
    },
    {
      title  = "Event API calls",
      period = local.metric_period,
      stat   = "Sum",
      metrics = [
        {
          name       = "API_CALLS.PublishEvent.count",
          dimensions = { success = true },
          attributes = { label = "PublishEvent" }
        },
        {
          name       = "API_CALLS.GetEvent.count",
          dimensions = { success = true },
          attributes = { label = "GetEvent" }
        },
        {
          name       = "API_CALLS.GetEvents.count",
          dimensions = { success = true },
          attributes = { label = "GetEvents" }
        },
        {
          name       = "API_CALLS.GetEventsStatus.count",
          dimensions = { success = true },
          attributes = { label = "GetEventsStatus" }
        },
        {
          name       = "API_CALLS.DeleteEvent.count",
          dimensions = { success = true },
          attributes = { label = "DeleteEvent" }
        },
      ]
    },
    {
      title  = "Data ingest calls",
      period = local.metric_period,
      stat   = "Sum",
      metrics = [
        {
          name       = "API_CALLS.PublishEvent.count",
          dimensions = { success = true },
          attributes = { label = "PublishEvent" }
        },
        {
          name       = "API_CALLS.CallsToLev.count",
          attributes = { label = "CallsToLev" }
        },
        {
          name       = "API_RESPONSES.ResponsesFromLev.count",
          attributes = { label = "ResponsesFromLev" }
        },
      ]
    },
    {
      title  = "Data processing times (p99)",
      period = local.metric_period,
      stat   = "p99",
      metrics = [
        {
          name       = "DATA_PROCESSING.TimeFromCreationToDeletion",
          attributes = { label = "Data creation to deletion time" }
        },
      ]
    }
  ]
}
