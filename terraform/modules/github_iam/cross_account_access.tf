data "aws_iam_policy_document" "cross_account_state_access" {
  statement {
    actions = [
      "s3:GetObject",
      "s3:ListObject",
      "s3:ListBucket",
    ]
    resources = [
      "arn:aws:s3:::${var.cross_account_bucket}",
      "arn:aws:s3:::${var.cross_account_bucket}/*"
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "cross_account_state_access" {
  name   = "github-oidc-cross-account-state-access"
  policy = data.aws_iam_policy_document.cross_account_state_access.json
}

resource "aws_iam_role_policy_attachment" "pr_cross_account_state_access" {
  role       = aws_iam_role.github_oidc_pull_request.name
  policy_arn = aws_iam_policy.cross_account_state_access.arn
}

resource "aws_iam_role_policy_attachment" "env_cross_account_state_access" {
  for_each = aws_iam_role.github_oidc_environment

  role       = each.value.name
  policy_arn = aws_iam_policy.cross_account_state_access.arn
}
