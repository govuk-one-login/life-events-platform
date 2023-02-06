data "aws_iam_policy_document" "github_oidc_environment_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [
        "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
      ]
    }

    condition {
      test     = "StringEquals"
      values   = ["sts.amazonaws.com"]
      variable = "token.actions.githubusercontent.com:aud"
    }

    condition {
      test     = "StringEquals"
      values   = ["repo:alphagov/gdx-data-share-poc:environment:${var.environment}"]
      variable = "token.actions.githubusercontent.com:sub"
    }
  }
}

resource "aws_iam_role" "github_oidc_environment" {
  name               = "${var.environment}-github-oidc-deploy"
  assume_role_policy = data.aws_iam_policy_document.github_oidc_environment_assume.json
}

data "aws_iam_policy" "github_oidc_environment" {
  name = "AdministratorAccess"
}

resource "aws_iam_role_policy_attachment" "github_oidc_environment" {
  role       = aws_iam_role.github_oidc_environment.name
  policy_arn = data.aws_iam_policy.github_oidc_environment.arn
}

data "aws_iam_policy_document" "github_oidc_pull_request_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"
      identifiers = [
        "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/token.actions.githubusercontent.com"
      ]
    }

    condition {
      test     = "StringEquals"
      values   = ["sts.amazonaws.com"]
      variable = "token.actions.githubusercontent.com:aud"
    }

    condition {
      test     = "StringLike"
      values   = ["repo:alphagov/gdx-data-share-poc:ref:refs/heads/*"]
      variable = "token.actions.githubusercontent.com:sub"
    }
  }
}

resource "aws_iam_role" "github_oidc_pull_request" {
  count = var.environment == "dev" ? 1 : 0

  name               = "github-oidc-pull-request"
  assume_role_policy = data.aws_iam_policy_document.github_oidc_pull_request_assume.json
}

data "aws_iam_policy" "github_oidc_pull_request" {
  name = "ReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "github_oidc_pull_request" {
  count = var.environment == "dev" ? 1 : 0

  role       = aws_iam_role.github_oidc_pull_request[0].name
  policy_arn = data.aws_iam_policy.github_oidc_pull_request.arn
}
