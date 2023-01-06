output "sns_topic_arn" {
  value = aws_sns_topic.topic.arn
}

output "sns_kms_key_arn" {
  value = aws_kms_key.sns_key.arn
}
