module "dwp_dev_queue" {
  source      = "../sqs"
  environment = "dev"
  queue_name  = "acq_dev_dwp-dev"
}

resource "aws_iam_user" "dwp_dev_user" {
  name = "dwp-dev"
}

resource "aws_iam_access_key" "dwp_dev_access_key" {
  user = aws_iam_user.dwp_dev_user.name
}

data "aws_iam_policy_document" "dwp_user_access" {
  statement {
    actions = [
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ReceiveMessage",
    ]
    resources = [
      module.dwp_dev_queue.queue_arn,
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [
      module.dwp_dev_queue.queue_kms_key_arn,
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "dwp_user_access" {
  policy = data.aws_iam_policy_document.dwp_user_access.json
  name   = "dwp-dev-sqs-access"
}

# We ignore MFA as this group will only include service accounts for access to SQS
#tfsec:ignore:aws-iam-enforce-mfa
resource "aws_iam_group" "dwp_dev" {
  name = "dwp-dev"
}

resource "aws_iam_user_group_membership" "dwp_dev" {
  user   = aws_iam_user.dwp_dev_user.name
  groups = [aws_iam_group.dwp_dev.name]
}

resource "aws_iam_group_policy_attachment" "dwp_group_access" {
  policy_arn = aws_iam_policy.dwp_user_access.arn
  group      = aws_iam_group.dwp_dev.name
}

data "aws_iam_policy_document" "dwp_queue_cross_account_access" {
  statement {
    actions = [
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ReceiveMessage",
    ]
    resources = [
      module.dwp_dev_queue.queue_arn,
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = [data.aws_ssm_parameter.dwp_principal.value]
    }
  }

  statement {
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [
      module.dwp_dev_queue.queue_kms_key_arn,
    ]
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = [data.aws_ssm_parameter.dwp_principal.value]
    }
  }
}

locals {
  dwp_queue_cross_account_access_policy_json = sensitive(data.aws_iam_policy_document.dwp_queue_cross_account_access.json)
}

resource "aws_sqs_queue_policy" "dwp_queue_cross_account_access" {
  policy    = local.dwp_queue_cross_account_access_policy_json
  queue_url = module.dwp_dev_queue.queue_id
}
