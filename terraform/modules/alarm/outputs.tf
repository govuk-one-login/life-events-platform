output "alarm_arn" {
  value = aws_cloudwatch_metric_alarm.error_rate_alarm.arn
}

output "alarm_description" {
  value = aws_cloudwatch_metric_alarm.error_rate_alarm.alarm_description
}
