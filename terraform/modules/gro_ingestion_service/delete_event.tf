resource "aws_iam_role" "delete_event_lambda" {
  name               = "${var.environment}-gro-ingestion-lambda-function-delete-event"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

data "aws_iam_policy_document" "delete_event_lambda" {
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
    sid = "DynamoDBDeleteItemAccess"
    actions = [
      "dynamodb:DeleteItem",
      "dynamodb:GetItem"
    ]
    resources = [
      aws_dynamodb_table.gro_ingestion.arn
    ]
  }
}

resource "aws_iam_policy" "delete_event_lambda" {
  name   = "${var.environment}-gro-ingestion-lambda-function-delete-event"
  policy = data.aws_iam_policy_document.delete_event_lambda.json
}

resource "aws_iam_role_policy_attachment" "delete_event_lambda" {
  policy_arn = aws_iam_policy.delete_event_lambda.arn
  role       = aws_iam_role.delete_event_lambda.name
}

resource "aws_iam_role_policy_attachment" "delete_event_lambda_xray_access" {
  policy_arn = data.aws_iam_policy.xray_access.arn
  role       = aws_iam_role.delete_event_lambda.name
}

resource "aws_iam_role_policy_attachment" "delete_event_lambda_logs_access" {
  policy_arn = aws_iam_policy.log_policy.arn
  role       = aws_iam_role.delete_event_lambda.name
}

resource "aws_cloudwatch_log_group" "delete_event_log" {
  name              = "/aws/lambda/${aws_lambda_function.delete_event_lambda.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.gro_ingestion.arn
}

resource "aws_lambda_function" "delete_event_lambda" {
  filename      = data.archive_file.lambda_function_source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-delete-event"

  handler       = "index.handler"
  runtime       = local.lambda_runtime
  role          = aws_iam_role.delete_event_lambda.arn
  timeout       = 10
  architectures = ["arm64"]

  source_code_hash = data.archive_file.lambda_function_source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME" = "deleteEvent"
      "TABLE_NAME"    = aws_dynamodb_table.gro_ingestion.name
    }
  }
  tracing_config {
    mode = "Active"
  }
}
