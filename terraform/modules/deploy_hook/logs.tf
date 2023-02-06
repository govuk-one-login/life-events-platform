data "aws_caller_identity" "current" {}

resource "aws_cloudwatch_log_group" "hook_log" {
  name              = "/aws/lambda/${aws_lambda_function.hook.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_kms_key" "log_key" {
  description         = "Key used to encrypt cloudfront and cloudwatch logs for deployment hook"
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

data "aws_iam_policy_document" "hook_log_policy" {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:AssociateKmsKey"
    ]
    resources = ["arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:*"]
  }
}

resource "aws_iam_policy" "hook_log_policy" {
  name   = "${var.environment}-hook-log-policy"
  policy = data.aws_iam_policy_document.hook_log_policy.json
}

resource "aws_iam_role_policy_attachment" "hook_log" {
  role       = aws_iam_role.hook.name
  policy_arn = aws_iam_policy.hook_log_policy.arn
}
