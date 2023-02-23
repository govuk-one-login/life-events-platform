locals {
  notification_emails = ["gdx-dev-team@digital.cabinet-office.gov.uk"]
}

data "aws_iam_policy_document" "sns_kms_access" {
  statement {
    sid = "__default_statement_ID"
    actions = [
      "kms:*",
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }
    resources = ["*"]
  }
  statement {
    sid = "__prometheus_statement_ID"
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

      values = [aws_prometheus_workspace.prometheus.arn]
    }
  }
}

resource "aws_kms_key" "sns_alarm_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sns alarm"
  policy              = data.aws_iam_policy_document.sns_kms_access.json
}

resource "aws_kms_alias" "sqs_key_alias" {
  name          = "alias/${var.environment}/sns-alarm-key"
  target_key_id = aws_kms_key.sns_alarm_key.arn
}

resource "aws_sns_topic" "sns_alarm_topic" {
  name              = "${var.environment}-gdx-alarms"
  kms_master_key_id = aws_kms_key.sns_alarm_key.arn
}

data "aws_iam_policy_document" "sns_access" {
  statement {
    sid = "__default_statement_ID"
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
    resources = [aws_sns_topic.sns_alarm_topic.arn]
    condition {
      test     = "StringEquals"
      variable = "AWS:SourceOwner"

      values = [data.aws_caller_identity.current.account_id]
    }
  }
  statement {
    sid = "__prometheus_statement_ID"
    actions = [
      "SNS:Publish",
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    resources = [aws_sns_topic.sns_alarm_topic.arn]
    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"

      values = [aws_prometheus_workspace.prometheus.arn]
    }
  }
}

resource "aws_sns_topic_policy" "sns_access" {
  arn = aws_sns_topic.sns_alarm_topic.arn

  policy = data.aws_iam_policy_document.sns_access.json
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  for_each  = toset(local.notification_emails)
  topic_arn = aws_sns_topic.sns_alarm_topic.arn
  protocol  = "email"
  endpoint  = each.key
}
