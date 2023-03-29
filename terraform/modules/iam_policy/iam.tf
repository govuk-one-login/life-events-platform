resource "aws_iam_group" "mfa_enforced" {
  name = "mfa_enforced"
}

data "aws_iam_policy_document" "enforce_mfa" {
  statement {
    sid    = "DenyAllExceptListedIfNoMFA"
    effect = "Deny"
    not_actions = [
      "iam:CreateVirtualMFADevice",
      "iam:EnableMFADevice",
      "iam:GetUser",
      "iam:ListMFADevices",
      "iam:ListVirtualMFADevices",
      "iam:ResyncMFADevice",
      "sts:GetSessionToken"
    ]
    resources = ["*"]
    condition {
      test     = "BoolIfExists"
      variable = "aws:MultiFactorAuthPresent"
      values   = ["false", ]
    }
  }
}

resource "aws_iam_policy" "enforce_mfa" {
  name   = "enforce-mfa-policy"
  policy = data.aws_iam_policy_document.enforce_mfa.json
}

resource "aws_iam_group_policy_attachment" "enforce_mfa" {
  group      = aws_iam_group.mfa_enforced.name
  policy_arn = aws_iam_policy.enforce_mfa.arn
}
