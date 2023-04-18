resource "aws_cloudwatch_log_metric_filter" "filter" {
  name           = var.name
  pattern        = var.filter_pattern
  log_group_name = var.log_group_name

  metric_transformation {
    name      = var.name
    namespace = "CloudtrailMetrics"
    value     = "1"
  }
}

resource "aws_cloudwatch_metric_alarm" "alarm" {
  alarm_name          = "cloudtrail-${var.name}"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  statistic           = "Sum"
  namespace           = "CloudtrailMetrics"
  metric_name         = var.name
  evaluation_periods  = 2
  period              = 300
  threshold           = 1
  alarm_description   = "Cloudtrail alarm for action ${var.name}"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [var.sns_topic_arn]
  ok_actions          = [var.sns_topic_arn]
}
