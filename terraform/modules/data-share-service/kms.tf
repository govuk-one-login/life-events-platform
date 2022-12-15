resource "aws_kms_key" "log_key" {
  description         = "Key used to encrypt cloudfront and cloudwatch logs"
  enable_key_rotation = true

  policy = data.aws_iam_policy_document.kms_log_key_policy.json
}

resource "aws_kms_alias" "log_key_alias" {
  name          = "alias/${var.environment}/log-key"
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

resource "aws_kms_key" "rds_key" {
  description         = "Key for RDS encryption"
  enable_key_rotation = true
}

resource "aws_kms_alias" "rds_key_alias" {
  name          = "alias/${var.environment}/rds-cluster"
  target_key_id = aws_kms_key.rds_key.key_id
}
