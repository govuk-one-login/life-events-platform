data "aws_iam_policy_document" "github_oidc_pull_request_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [
        "arn:aws:iam::${var.account_id}:oidc-provider/token.actions.githubusercontent.com"
      ]
    }

    condition {
      test     = "StringEquals"
      values   = ["sts.amazonaws.com"]
      variable = "token.actions.githubusercontent.com:aud"
    }

    condition {
      test = "StringLike"
      values = [
        "repo:alphagov/di-data-life-events-platform:pull_request",
        "repo:alphagov/di-data-life-events-platform:ref:refs/heads/gh-readonly-queue/main/pr*"
      ]
      variable = "token.actions.githubusercontent.com:sub"
    }
  }
}

resource "aws_iam_role" "github_oidc_pull_request" {
  name               = "github-oidc-pull-request"
  assume_role_policy = data.aws_iam_policy_document.github_oidc_pull_request_assume.json
}

data "aws_dynamodb_table" "terraform_lock" {
  name = var.terraform_lock_table_name
}

data "aws_iam_policy_document" "github_oidc_pull_request_state" {
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

resource "aws_iam_policy" "github_oidc_pull_request_state" {
  name   = "github-oidc-pull-request-state"
  policy = data.aws_iam_policy_document.github_oidc_pull_request_state.json
}

resource "aws_iam_role_policy_attachment" "github_oidc_pull_request_state" {
  role       = aws_iam_role.github_oidc_pull_request.name
  policy_arn = aws_iam_policy.github_oidc_pull_request_state.arn
}

data "aws_iam_policy" "github_oidc_pull_request_readonly" {
  name = "ReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "github_oidc_pull_request_readonly" {
  role       = aws_iam_role.github_oidc_pull_request.name
  policy_arn = data.aws_iam_policy.github_oidc_pull_request_readonly.arn
}
