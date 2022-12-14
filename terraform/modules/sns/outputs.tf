output "sns_topic_arn" {
  value = aws_sns_topic.topic.arn
}

output "access_key_id" {
  value = aws_iam_access_key.user.id
}

output "access_key_secret" {
  value = aws_iam_access_key.user.secret
}
