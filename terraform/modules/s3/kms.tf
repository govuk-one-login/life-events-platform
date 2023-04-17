data "aws_iam_policy_document" "cloudtrail_kms_access" {
  statement {
    sid    = "Allow cloudtrail logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudtrail.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "kms_policy" {
  source_policy_documents = [var.allow_cloudtrail_logs != 0 ? data.aws_iam_policy_document.cloudtrail_kms_access.json : ""]

  statement {
    sid    = "Enable IAM User Permissions"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    sid    = "Allow delivery logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["delivery.logs.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["logs.${var.region}.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]
    resources = ["*"]
  }
}

resource "aws_kms_key" "bucket" {
  count = var.use_kms ? 1 : 0

  enable_key_rotation = true
  description         = "Key used to encrypt state bucket"

  policy = data.aws_iam_policy_document.kms_policy.json
}

resource "aws_kms_alias" "bucket_alias" {
  count = var.use_kms ? 1 : 0

  name          = "alias/${var.prefix}${var.prefix == "" ? "" : "/"}${var.name}-bucket-key"
  target_key_id = aws_kms_key.bucket[0].arn
}
