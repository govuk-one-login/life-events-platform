resource "aws_lambda_event_source_mapping" "dynamo_stream" {
  event_source_arn  = aws_dynamodb_table.gro_ingestion.stream_arn
  function_name     = aws_lambda_function.publish_event_lambda.function_name
  starting_position = "LATEST"
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

data "aws_iam_policy_document" "publish_event_lambda" {
  statement {
    sid = "LambdaKMSAccess"
    actions = [
      "kms:Decrypt",
      "kms:Encrypt",
      "kms:GenerateDataKey",
      "kms:GenerateDataKeyPair",
      "kms:ReEncryptFrom",
      "kms:ReEncryptTo"
    ]
    effect = "Allow"
    resources = [
      aws_kms_key.gro_ingestion.arn
    ]
  }

  statement {
    sid = "DynamoDBGetItemAccess"
    actions = [
      "dynamodb:GetItem"
    ]
    resources = [
      aws_dynamodb_table.gro_ingestion.arn
    ]
  }

  statement {
    sid = "DynamoDBStreamsAccess"
    actions = [
      "dynamodb:DescribeStream",
      "dynamodb:GetRecords",
      "dynamodb:GetShardIterator",
      "dynamodb:ListStreams"
    ]
    resources = [
      aws_dynamodb_table.gro_ingestion.stream_arn
    ]
  }
}

resource "aws_iam_policy" "publish_event_lambda" {
  name   = "${var.environment}-gro-ingestion-lambda-function-publish-event"
  policy = data.aws_iam_policy_document.publish_event_lambda.json
}

resource "aws_iam_role_policy_attachment" "publish_event_lambda" {
  policy_arn = aws_iam_policy.publish_event_lambda.arn
  role       = aws_iam_role.publish_event_lambda.name
}

resource "aws_iam_role_policy_attachment" "publish_event_lambda_xray_access" {
  policy_arn = data.aws_iam_policy.xray_access.arn
  role       = aws_iam_role.publish_event_lambda.name
}

resource "aws_iam_role_policy_attachment" "publish_event_lambda_logs_access" {
  policy_arn = aws_iam_policy.log_policy.arn
  role       = aws_iam_role.publish_event_lambda.name
}

resource "aws_cloudwatch_log_group" "publish_event_log" {
  name              = "/aws/lambda/${aws_lambda_function.publish_event_lambda.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.gro_ingestion.arn
}

resource "aws_lambda_function" "publish_event_lambda" {
  filename      = data.archive_file.lambda_function_source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-publish-event"

  handler = "index.handler"
  runtime = local.lambda_runtime
  role    = aws_iam_role.publish_event_lambda.arn
  timeout = 10

  source_code_hash = data.archive_file.lambda_function_source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME" = "publishEvent"
      "GDX_URL"       = var.gdx_url
      "AUTH_URL"      = var.auth_url
      "CLIENT_ID"     = var.publisher_client_id
      "CLIENT_SECRET" = var.publisher_client_secret
    }
  }
  tracing_config {
    mode = "Active"
  }
}
