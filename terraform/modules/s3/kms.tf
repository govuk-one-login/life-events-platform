data "aws_iam_policy_document" "cloudtrail_kms_access_policy" {
  statement {
    sid    = "Allow cloudtrail KMS access"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudtrail.amazonaws.com"]
    }
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey*",
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "delivery_log_kms_access_policy" {
  statement {
    sid    = "Allow delivery KMS access"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["delivery.logs.amazonaws.com"]
    }
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey*",
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "config_kms_access_policy" {
  statement {
    sid    = "Allow Config KMS access"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["config.amazonaws.com"]
    }
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey*",
    ]
    resources = ["*"]
  }
}

locals {
  cloudtrail_kms_policy    = var.allow_cloudtrail_logs ? [data.aws_iam_policy_document.cloudtrail_kms_access_policy.json] : []
  delivery_log_kms_policy  = var.allow_delivery_logs ? [data.aws_iam_policy_document.delivery_log_kms_access_policy.json] : []
  config_kms_policy        = var.allow_config_logs ? [data.aws_iam_policy_document.config_kms_access_policy.json] : []
  source_kms_policies  = concat(
    local.cloudtrail_kms_policy,
    local.delivery_log_kms_policy,
    local.config_kms_policy
  )
}

data "aws_iam_policy_document" "kms_policy" {
  source_policy_documents = local.source_kms_policies

  statement {
    sid    = "Enable S3 logging permissions"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey*",
    ]
    resources = ["*"]
  }

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
