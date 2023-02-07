data "archive_file" "hook_lambda" {
  type = "zip"
  source {
    content  = file("${path.module}/lambdas/hook.py")
    filename = "hook.py"
  }
  source {
    content  = file("${path.module}/../lambdas/common.py")
    filename = "common.py"
  }
  output_path = "${path.module}/zip/hook.zip"
}

resource "aws_lambda_function" "hook" {
  function_name    = "${var.environment}-codedeploy-before-traffic-hook"
  handler          = "hook.lambda_handler"
  role             = aws_iam_role.hook.arn
  runtime          = "python3.9"
  filename         = data.archive_file.hook_lambda.output_path
  source_code_hash = data.archive_file.hook_lambda.output_base64sha256
  timeout          = 300

  environment {
    variables = {
      test_gdx_url  = var.test_gdx_url
      auth_url      = var.auth_url
      client_id     = var.client_id
      client_secret = var.client_secret
    }
  }
  tracing_config {
    mode = "Active"
  }
  vpc_config {
    security_group_ids = [var.security_group_id]
    subnet_ids         = var.subnet_ids
  }
}

data "aws_iam_policy_document" "hook_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "hook" {
  name               = "${var.environment}-deploy-hook"
  assume_role_policy = data.aws_iam_policy_document.hook_assume_policy.json
}

resource "aws_iam_role_policy_attachment" "hook_xray_access" {
  role       = aws_iam_role.hook.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}

data "aws_iam_policy_document" "hook_codedeploy" {
  statement {
    effect = "Allow"
    actions = [
      "codedeploy:PutLifecycleEventHookExecutionStatus"
    ]
    resources = [var.codedeploy_arn]
  }
}

resource "aws_iam_policy" "hook_codedeploy" {
  name   = "${var.environment}-hook-codedeploy"
  policy = data.aws_iam_policy_document.hook_codedeploy.json
}

resource "aws_iam_role_policy_attachment" "hook_codedeploy" {
  role       = aws_iam_role.hook.name
  policy_arn = aws_iam_policy.hook_codedeploy.arn
}

