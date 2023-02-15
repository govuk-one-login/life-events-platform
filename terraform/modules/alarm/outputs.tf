output "alarm_arn" {
  value = aws_cloudwatch_metric_alarm.error_rate_alarms.arn
}

output "alarm_description" {
  value = aws_cloudwatch_metric_alarm.error_rate_alarms.alarm_description
}
