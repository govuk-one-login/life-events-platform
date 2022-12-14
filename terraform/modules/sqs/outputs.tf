output "queue_name" {
  value = aws_sqs_queue.queue.name
}

output "queue_access_key_id" {
  value = aws_iam_access_key.sqs_user.id
}

output "queue_access_key_secret" {
  value = aws_iam_access_key.sqs_user.secret
}

output "dead_letter_queue_name" {
  value = aws_sqs_queue.dead_letter_queue.name
}

output "dead_letter_queue_access_key_id" {
  value = aws_iam_access_key.sqs_user.id
}

output "dead_letter_queue_access_key_secret" {
  value = aws_iam_access_key.sqs_user.secret
}
