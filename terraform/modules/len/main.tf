data "archive_file" "len_lambda" {
  type        = "zip"
  source_dir  = "${path.module}/lambdas"
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
      gdx_url             = var.gdx_url
      auth_url            = var.auth_url
      len_client_id       = var.len_client_id
      len_client_secret   = var.len_client_secret
      lev_rds_db_username = var.lev_rds_db_username
      lev_rds_db_password = var.lev_rds_db_password
      lev_rds_db_name     = var.lev_rds_db_name
      lev_rds_db_host     = var.lev_rds_db_host
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

resource "aws_iam_role_policy_attachment" "len_xray_access" {
  role       = aws_iam_role.len.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}

resource "aws_cloudwatch_event_rule" "schedule" {
  name                = "${var.environment}LENLambdaSchedule"
  description         = "Schedule for Lambda Function to send LEN events"
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
