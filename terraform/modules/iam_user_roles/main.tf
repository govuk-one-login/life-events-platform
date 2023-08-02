data "aws_iam_policy" "security_hub_read_only_policy" {
  name = "AWSSecurityHubReadOnlyAccess"
}

data "aws_iam_policy" "support_access_policy" {
  name = "AWSSupportAccess"
}

data "aws_dynamodb_table" "terraform_lock" {
  name = var.terraform_lock_table_name
}

data "aws_iam_policy_document" "terraform_plan_policy" {
  statement {
    actions = [
      "dynamodb:PutItem",
      "dynamodb:DeleteItem"
    ]
    resources = [
      data.aws_dynamodb_table.terraform_lock.arn
    ]
    effect = "Allow"
  }
  statement {
    actions = [
      "aps:DescribeLoggingConfiguration"
    ]
    resources = ["arn:aws:aps:eu-west-2:${var.account_id}:workspace/*"]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "terraform_plan_policy" {
  name   = "readonly-terraform-plan-policy"
  policy = data.aws_iam_policy_document.terraform_plan_policy.json
}

