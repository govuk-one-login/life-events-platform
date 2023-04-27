resource "aws_kms_key" "gro_ingestion" {
  description         = "Encryption key for GRO ingestion"
  enable_key_rotation = true

  policy = data.aws_iam_policy_document.kms_log_key_policy.json
}

resource "aws_kms_alias" "gro_ingestion" {
  name          = "alias/${var.environment}/gro-ingestion-key"
  target_key_id = aws_kms_key.gro_ingestion.arn
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
