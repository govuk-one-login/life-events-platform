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
      "sqs:DeleteMessage",
      "sqs:ReceiveMessage",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility",
      "sqs:GetQueueAttributes",
    ]
    resources = [
      module.dwp_dev_queue.queue_arn,
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
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

resource "aws_iam_group" "dwp_dev" {
  name = "dwp-dev"
}

resource "aws_iam_user_group_membership" "dwp_dev" {
  user = aws_iam_user.dwp_dev_user.name
  groups = [aws_iam_group.dwp_dev.name]
}

resource "aws_iam_group_policy_attachment" "dwp_group_access" {
  policy_arn = aws_iam_policy.dwp_user_access.arn
  group      = aws_iam_group.dwp_dev.name
}
