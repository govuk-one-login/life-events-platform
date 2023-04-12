data "aws_iam_policy_document" "kms_prometheus_access" {
  statement {
    sid = "Prometheus SNS KMS Access"
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    resources = ["*"]
    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"

      values = [var.prometheus_arn]
    }
  }
}

data "aws_iam_policy_document" "kms_s3_access" {
  statement {
    sid = "S3 SNS KMS Access"
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["s3.amazonaws.com"]
    }
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceAccount"

      values = [var.account_id]
    }
  }
}

locals {
  kms_prometheus_policy = var.prometheus_arn != null ? [data.aws_iam_policy_document.kms_prometheus_access.json] : []
  kms_s3_policy         = var.allow_s3_notification ? [data.aws_iam_policy_document.kms_s3_access.json] : []
  kms_source_policies   = concat(local.kms_prometheus_policy, local.kms_s3_policy)
}

data "aws_iam_policy_document" "kms_access" {
  source_policy_documents = local.kms_source_policies

  statement {
    sid = "SNS KMS Access"
    actions = [
      "kms:*",
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
    }
    resources = ["*"]
  }
}

resource "aws_kms_key" "sns_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt SNS ${var.environment}-${var.name}"
  policy              = data.aws_iam_policy_document.kms_access.json
}

resource "aws_kms_alias" "sns_key_alias" {
  name          = "alias/${var.environment}/sns-${var.name}-key"
  target_key_id = aws_kms_key.sns_key.arn
}

resource "aws_sns_topic" "sns_topic" {
  name              = "${var.environment}-${var.name}"
  kms_master_key_id = aws_kms_key.sns_key.arn
}

data "aws_iam_policy_document" "prometheus_access" {
  statement {
    sid = "Prometheus SNS Access"
    actions = [
      "SNS:Publish",
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    resources = [aws_sns_topic.sns_topic.arn]
    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"

      values = [var.prometheus_arn]
    }
  }
}

data "aws_iam_policy_document" "s3_access" {
  statement {
    sid = "S3 SNS Access"
    actions = [
      "SNS:Publish",
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["s3.amazonaws.com"]
    }
    resources = ["arn:aws:sns:*:*:s3-event-notification-topic"]
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceAccount"

      values = [var.account_id]
    }
  }
}

locals {
  prometheus_policy = var.prometheus_arn != null ? [data.aws_iam_policy_document.prometheus_access.json] : []
  s3_policy         = var.allow_s3_notification ? [data.aws_iam_policy_document.s3_access.json] : []
  source_policies   = concat(local.prometheus_policy, local.s3_policy)
}

data "aws_iam_policy_document" "sns_access" {
  source_policy_documents = local.source_policies

  statement {
    actions = [
      "SNS:Subscribe",
      "SNS:SetTopicAttributes",
      "SNS:RemovePermission",
      "SNS:Receive",
      "SNS:Publish",
      "SNS:ListSubscriptionsByTopic",
      "SNS:GetTopicAttributes",
      "SNS:DeleteTopic",
      "SNS:AddPermission",
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    resources = [aws_sns_topic.sns_topic.arn]
    condition {
      test     = "StringEquals"
      variable = "AWS:SourceOwner"

      values = [var.account_id]
    }
  }
}

resource "aws_sns_topic_policy" "sns_access" {
  arn = aws_sns_topic.sns_topic.arn

  policy = data.aws_iam_policy_document.sns_access.json
}

resource "aws_sns_topic_subscription" "notification_emails" {
  for_each  = toset(var.notification_emails)
  topic_arn = aws_sns_topic.sns_topic.arn
  protocol  = "email"
  endpoint  = each.key
}
