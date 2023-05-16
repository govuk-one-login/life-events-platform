output "delete_event_function_name" {
  value = aws_lambda_function.delete_event_lambda.function_name
}

output "enrich_event_function_name" {
  value = aws_lambda_function.enrich_event_lambda.function_name
}
