output "topic_arn" {
  value = aws_sns_topic.sns_topic.arn
}
output "kms_arn" {
  value = aws_kms_key.sns_key.arn
}

