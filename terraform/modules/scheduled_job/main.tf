data "aws_caller_identity" "current" {}

data "archive_file" "len_lambda" {
  type        = "zip"
  source_file = "${path.module}/lambdas/len.py"
  output_path = "${path.module}/zip/len.zip"
}

resource "aws_lambda_function" "len" {
  function_name    = "${var.environment}-len"
  handler          = "len.lambda_handler"
  role             = aws_iam_role.len.arn
  runtime          = "python3.9"
  filename         = data.archive_file.len_lambda.output_path
  source_code_hash = data.archive_file.len_lambda.output_base64sha256
  timeout          = 10

  environment {
    variables = {
      environment = var.environment
      region      = var.region
    }
  }
  tracing_config {
    mode = "Active"
  }
}

resource "aws_cloudwatch_log_group" "len_log" {
  name              = "/aws/lambda/${aws_lambda_function.len.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_kms_key" "log_key" {
  description         = "Key used to encrypt cloudfront and cloudwatch logs for LEN"
  enable_key_rotation = true

  policy = data.aws_iam_policy_document.kms_log_key_policy.json
}

resource "aws_kms_alias" "log_key_alias" {
  name          = "alias/${var.environment}/len-log-key"
  target_key_id = aws_kms_key.log_key.arn
}

data "aws_iam_policy_document" "kms_log_key_policy" {
  statement {
    effect = "Allow"
    sid    = "Enable IAM User Permissions"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["logs.${var.region}.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt*",
      "kms:Decrypt*",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:Describe*"
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "len_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "len" {
  name               = "${var.environment}-len"
  assume_role_policy = data.aws_iam_policy_document.len_assume_policy.json
}

data "aws_iam_policy_document" "len_log_policy" {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:AssociateKmsKey"
    ]
    resources = [aws_cloudwatch_log_group.len_log.arn]
  }
}

resource "aws_iam_policy" "len_log_policy" {
  name   = "${var.environment}-len_log_policy"
  policy = data.aws_iam_policy_document.len_log_policy.json
}

resource "aws_iam_role_policy_attachment" "len_log" {
  role       = aws_iam_role.len.name
  policy_arn = aws_iam_policy.len_log_policy.arn
}

resource "aws_cloudwatch_event_rule" "schedule" {
  name                = "schedule"
  description         = "Schedule for Lambda Function"
  schedule_expression = var.schedule
}

resource "aws_cloudwatch_event_target" "schedule_lambda" {
  rule      = aws_cloudwatch_event_rule.schedule.name
  target_id = "len_lambda"
  arn       = aws_lambda_function.len.arn
}

resource "aws_lambda_permission" "allow_events_bridge_to_run_lambda" {
  statement_id  = "AllowExecutionFromCloudWatch"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.len.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.schedule.arn
}
