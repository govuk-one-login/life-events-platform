data "archive_file" "get_events_lambda" {
  type = "zip"
  source {
    content  = file("${path.module}/lambdas/get_events.py")
    filename = "get_events.py"
  }
  source {
    content  = file("${path.module}/../lambdas/common.py")
    filename = "common.py"
  }
  output_path = "${path.module}/zip/getEvents.zip"
}

resource "aws_lambda_function" "get_events" {
  function_name    = "${var.environment}-get-events"
  handler          = "get_events.lambda_handler"
  role             = aws_iam_role.get_events.arn
  runtime          = "python3.9"
  filename         = data.archive_file.get_events_lambda.output_path
  source_code_hash = data.archive_file.get_events_lambda.output_base64sha256
  timeout          = 10

  environment {
    variables = {
      gdx_url                     = var.gdx_url
      auth_url                    = var.auth_url
      client_id                   = var.consumer_client_id
      client_secret               = var.consumer_client_secret
      cloudwatch_metric_namespace = "${var.environment}-example-consumer"
    }
  }
  tracing_config {
    mode = "Active"
  }
}

resource "aws_iam_role" "get_events" {
  name               = "${var.environment}-get-events"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

resource "aws_iam_role_policy_attachment" "get_events_xray_access" {
  role       = aws_iam_role.get_events.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}


resource "aws_iam_role_policy_attachment" "get_events_log_access" {
  role       = aws_iam_role.get_events.name
  policy_arn = aws_iam_policy.log_policy.arn
}

resource "aws_cloudwatch_log_group" "get_events_log" {
  name              = "/aws/lambda/${aws_lambda_function.get_events.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_iam_role_policy_attachment" "get_events_put_metrics" {
  policy_arn = aws_iam_policy.allow_cloudwatch_metrics.arn
  role       = aws_iam_role.get_events.name
}
