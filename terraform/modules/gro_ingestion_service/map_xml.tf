resource "aws_iam_role" "map_xml_lambda" {
  name               = "${var.environment}-gro-ingestion-lambda-function-map-xml"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

data "aws_iam_policy_document" "map_xml_lambda" {
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
    sid = "DynamoDBPutItemAccess"
    actions = [
      "dynamodb:PutItem"
    ]
    resources = [
      aws_dynamodb_table.gro_ingestion.arn
    ]
  }
}

resource "aws_iam_policy" "map_xml_lambda" {
  name   = "${var.environment}-gro-ingestion-lambda-function-map-xml"
  policy = data.aws_iam_policy_document.publish_event_lambda.json
}

resource "aws_iam_role_policy_attachment" "map_xml_lambda" {
  policy_arn = aws_iam_policy.map_xml_lambda.arn
  role       = aws_iam_role.map_xml_lambda.name
}

resource "aws_iam_role_policy_attachment" "map_xml_lambda_xray_access" {
  policy_arn = data.aws_iam_policy.xray_access.arn
  role       = aws_iam_role.map_xml_lambda.name
}

resource "aws_iam_role_policy_attachment" "map_xml_lambda_logs_access" {
  policy_arn = aws_iam_policy.log_policy.arn
  role       = aws_iam_role.publish_event_lambda.name
}

resource "aws_cloudwatch_log_group" "map_xml_log" {
  name              = "/aws/lambda/${aws_lambda_function.map_xml_lambda.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.gro_ingestion.arn
}

resource "aws_lambda_function" "map_xml_lambda" {
  filename      = data.archive_file.lambda_function_source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-map-xml"

  handler = "index.handler"
  runtime = local.lambda_runtime
  role    = aws_iam_role.publish_event_lambda.arn
  timeout = 10

  source_code_hash = data.archive_file.lambda_function_source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME" = "mapXml"
      "TABLE_NAME"    = aws_dynamodb_table.gro_ingestion.name
    }
  }
  tracing_config {
    mode = "Active"
  }
}
