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
      "arn:aws:sqs:eu-west-2:776473272850:acq_dev_dwp-death-notifications"
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [
      "arn:aws:kms:eu-west-2:776473272850:key/8dc17eba-9e8f-44a6-8e11-c97c94acf526"
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
