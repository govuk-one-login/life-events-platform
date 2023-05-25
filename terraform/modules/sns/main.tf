data "aws_iam_policy_document" "kms_arns_access" {
  statement {
    sid = "ARN SNS KMS Access"
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

      values = var.arns_which_can_publish
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

data "aws_iam_policy_document" "kms_codestar_access" {
  statement {
    sid = "CodeStar SNS KMS Access"
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["codestar-notifications.amazonaws.com"]
    }
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"

      values = ["sns.${var.region}.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "kms_eventbridge_access" {
  statement {
    sid = "EventBridge SNS KMS Access"
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
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
  kms_prometheus_policy = length(var.arns_which_can_publish) != 0 ? [data.aws_iam_policy_document.kms_arns_access.json] : []
  kms_s3_policy         = var.allow_s3_notification ? [data.aws_iam_policy_document.kms_s3_access.json] : []
  kms_codestar_policy   = var.allow_codestar_notification ? [data.aws_iam_policy_document.kms_codestar_access.json] : []
  kms_eventbridge_policy = var.allow_eventbridge_notification ? [data.aws_iam_policy_document.kms_eventbridge_access.json] : []
  kms_source_policies   = concat(local.kms_prometheus_policy, local.kms_s3_policy, local.kms_codestar_policy, local.kms_eventbridge_policy)
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

data "aws_iam_policy_document" "arns_access" {
  statement {
    sid = "ARN SNS Access"
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

      values = var.arns_which_can_publish
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
    resources = [aws_sns_topic.sns_topic.arn]
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceAccount"

      values = [var.account_id]
    }
  }
}

data "aws_iam_policy_document" "codestar_access" {
  statement {
    sid = "CodeStar SNS Access"
    actions = [
      "SNS:Publish",
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["codestar-notifications.amazonaws.com"]
    }
    resources = [aws_sns_topic.sns_topic.arn]
  }
}

data "aws_iam_policy_document" "eventbridge_access" {
  statement {
    sid = "EventBridge SNS Access"
    actions = [
      "SNS:Publish",
    ]
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }
    resources = [aws_sns_topic.sns_topic.arn]
  }
}

locals {
  prometheus_policy = length(var.arns_which_can_publish) != 0 ? [data.aws_iam_policy_document.arns_access.json] : []
  s3_policy         = var.allow_s3_notification ? [data.aws_iam_policy_document.s3_access.json] : []
  codestar_policy   = var.allow_codestar_notification ? [data.aws_iam_policy_document.codestar_access.json] : []
  eventbridge_policy = var.allow_eventbridge_notification ? [data.aws_iam_policy_document.eventbridge_access.json] : []
  source_policies   = concat(local.prometheus_policy, local.s3_policy, local.codestar_policy, local.eventbridge_policy)
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
