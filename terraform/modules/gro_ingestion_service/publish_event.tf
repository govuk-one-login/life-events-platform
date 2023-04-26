resource "aws_lambda_event_source_mapping" "dynamo_stream" {
  event_source_arn = aws_dynamodb_table.gro_ingestion.stream_arn
  function_name = aws_lambda_function.publish_event_lambda.function_name
  filter_criteria {
    filter {
      pattern = "{ \"eventName\": [ \"INSERT\" ] }"
    }
  }
}

resource "aws_iam_role" "publish_event_lambda" {
  name               = "${var.environment}-gro-ingestion-lambda-function-publish-event"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

resource "aws_lambda_function" "publish_event_lambda" {
  filename      = data.archive_file.lambda_function_source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-publish-event"

  handler = "index.handler"
  runtime = local.lambda_runtime
  role    = aws_iam_role.publish_event_lambda
  timeout = 10

  source_code_hash = data.archive_file.lambda_function_source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME" = "publishEvent"
      "GDX_URL" = var.gdx_url
      "AUTH_URL" = var.auth_url
      "CLIENT_ID" = var.publisher_client_id
      "CLIENT_SECRET" = var.publisher_client_secret
    }
  }
}
