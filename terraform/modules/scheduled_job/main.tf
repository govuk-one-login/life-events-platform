
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

data "aws_iam_policy_document" "len_policy" {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:AssociateKmsKey"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "len_policy" {
  name   = "${var.environment}-len-policy"
  policy = data.aws_iam_policy_document.len_policy.json
}

resource "aws_iam_role_policy_attachment" "len" {
  role       = aws_iam_role.len.name
  policy_arn = aws_iam_policy.len_policy.arn
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
}
