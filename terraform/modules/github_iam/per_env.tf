locals {
  environments = toset(var.environments)
}

data "aws_iam_policy_document" "github_oidc_environment_assume" {
  for_each = local.environments

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
      test     = "StringEquals"
      values   = ["repo:alphagov/di-data-life-events-platform:environment:${each.value}"]
      variable = "token.actions.githubusercontent.com:sub"
    }
  }
}

resource "aws_iam_role" "github_oidc_environment" {
  for_each = local.environments

  name               = "${each.value}-github-oidc-deploy"
  assume_role_policy = data.aws_iam_policy_document.github_oidc_environment_assume[each.key].json
}

data "aws_iam_policy" "github_oidc_environment" {
  name = "AdministratorAccess"
}

resource "aws_iam_role_policy_attachment" "github_oidc_environment" {
  for_each = local.environments

  role       = aws_iam_role.github_oidc_environment[each.key].name
  policy_arn = data.aws_iam_policy.github_oidc_environment.arn
}
