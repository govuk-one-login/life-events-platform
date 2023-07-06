module "lambda_function_suppressor" {
  source  = "terraform-aws-modules/lambda/aws"
  version = "5.2.0"

  function_name = "${var.environment}-securityhub-suppressor"
  handler       = "entry.lambda_handler"
  runtime       = "python3.9"
  lambda_role   = aws_iam_role.securityhub_suppressor.arn

  timeout = 60

  environment_variables = {
    DYNAMODB_TABLE_NAME         = aws_dynamodb_table.suppressor_dynamodb_table.name
    LOG_LEVEL                   = "INFO"
    POWERTOOLS_LOGGER_LOG_EVENT = "false"
    POWERTOOLS_SERVICE_NAME     = "securityhub-suppressor"
  }

  tracing_mode = "Active"

  create_role = false

  cloudwatch_logs_retention_in_days = var.lambda_cloudwatch_retention_days
  cloudwatch_logs_kms_key_id        = aws_kms_key.security_hub_findings.arn

  source_path = "${path.module}/lambdas/"
}

data "aws_iam_policy_document" "securityhub_suppressor_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "securityhub_suppressor" {
  name               = "${var.environment}-securityhub-suppressor"
  assume_role_policy = data.aws_iam_policy_document.securityhub_suppressor_assume_policy.json
}

data "aws_iam_policy_document" "lambda_key_policy" {
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
      aws_kms_key.security_hub_findings.arn
    ]
  }
}

data "aws_iam_policy_document" "lambda_security_hub_suppressor" {
  source_policy_documents = [data.aws_iam_policy_document.lambda_key_policy.json]

  statement {
    sid = "TrustEventsToStoreLogEvent"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:DescribeLogStreams",
      "logs:PutLogEvents"
    ]
    resources = ["arn:aws:logs:${var.region}:${var.account_id}:log-group:*"]
  }

  statement {
    sid = "DynamoDBGetItemAccess"
    actions = [
      "dynamodb:GetItem"
    ]
    resources = [
      aws_dynamodb_table.suppressor_dynamodb_table.arn
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
      aws_dynamodb_table.suppressor_dynamodb_table.stream_arn
    ]
  }

  statement {
    sid = "SecurityHubAccess"
    actions = [
      "securityhub:BatchUpdateFindings",
      "securityhub:GetFindings"
    ]
    resources = [
      "arn:aws:securityhub:${var.region}:${var.account_id}:hub/default"
    ]
  }
}

resource "aws_iam_policy" "securityhub_suppressor" {
  name   = "${var.environment}-securityhub-suppressor"
  policy = data.aws_iam_policy_document.lambda_security_hub_suppressor.json
}

resource "aws_iam_role_policy_attachment" "securityhub_suppressor" {
  policy_arn = aws_iam_policy.securityhub_suppressor.arn
  role       = aws_iam_role.securityhub_suppressor.name
}
