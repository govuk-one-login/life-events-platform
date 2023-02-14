locals {
  metric_period = 300
  metric_colours = {
    green = "#2ca02c"
    red   = "#d62728"
  }
}

data "aws_iam_policy_document" "allow_cloudwatch_metrics" {
  statement {
    effect    = "Allow"
    actions   = ["cloudwatch:PutMetricData"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "allow_cloudwatch_metrics" {
  policy = data.aws_iam_policy_document.allow_cloudwatch_metrics.json
  name   = "${var.environment}-allow-cloudwatch-metrics"
}

module "metrics_dashboard" {
  source           = "../cloudwatch_metrics_dashboard"
  region           = var.region
  dashboard_name   = "${var.environment}-example-consumer-metrics-dashboard"
  metric_namespace = "${var.environment}-example-consumer"
  widgets = [
    {
      title  = "Get Events"
      period = local.metric_period
      stat   = "Average"
      metrics = [
        { name = "GET_EVENTS.Count", attributes = { label = "Number of events retrieved" } },
        { name = "GET_EVENTS.Duration", attributes = { label = "Call duration", yAxis = "right" } }
      ]
    },
    {
      title  = "Data matching"
      period = local.metric_period
      stat   = "Sum"
      metrics = [
        { name = "GET_EVENT.DataMatchSuccess", attributes = { color = local.metric_colours.green } },
        { name = "GET_EVENT.DataMatchFailure", attributes = { color = local.metric_colours.red } }
      ]
    },
    {
      title  = "Get Event"
      period = local.metric_period
      stat   = "Average"
      metrics = [
        { name = "GET_EVENT.Duration", attributes = { label = "Call duration" } }
      ]
    },
    {
      title  = "Delete Event"
      period = local.metric_period
      stat   = "Average"
      metrics = [
        { name = "DELETE_EVENT.Duration", attributes = { label = "Call duration" } }
      ]
    }
  ]
}
