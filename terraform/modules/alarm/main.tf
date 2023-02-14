resource "aws_cloudwatch_metric_alarm" "error_rate_alarm" {
  alarm_name          = var.alarm_name
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  threshold           = "10"
  alarm_description   = var.alarm_description
  treat_missing_data  = "notBreaching"
  alarm_actions       = [var.alarm_action]
  ok_actions          = [var.alarm_action]

  metric_query {
    id          = "error_rate"
    expression  = "IF(successful_requests + errors > 10, 100*errors/(successful_requests + errors), 0)"
    label       = "Error Rate"
    return_data = "true"
  }

  metric_query {
    id = "errors"
    metric {
      metric_name = var.error_metric.name
      namespace   = var.error_metric.namespace
      period      = "300"
      stat        = "Sum"

      dimensions = var.error_metric.dimensions
    }
  }

  metric_query {
    id = "successful_requests"
    metric {
      metric_name = var.success_metric.name
      namespace   = var.success_metric.namespace
      period      = "300"
      stat        = "Sum"

      dimensions = var.success_metric.dimensions
    }
  }
}
