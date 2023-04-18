locals {
  admin_users     = toset(var.admin_users)
  read_only_users = toset(var.read_only_users)
}

data "aws_iam_policy" "admin_policy" {
  name = "AdministratorAccess"
}

data "aws_iam_policy" "read_only_policy" {
  name = "ReadOnlyAccess"
}

data "aws_iam_policy" "security_hub_read_only_policy" {
  name = "AWSSecurityHubReadOnlyAccess"
}

data "aws_iam_policy" "support_access_policy" {
  name = "AWSSupportAccess"
}

module "admin_roles" {
  for_each = local.admin_users

  source      = "../iam_user_role"
  role_suffix = "admin"
  username    = each.value
  policy_arns = [data.aws_iam_policy.admin_policy.arn]
}

module "read_only_roles" {
  for_each = local.read_only_users

  source      = "../iam_user_role"
  role_suffix = "read-only"
  username    = each.value
  policy_arns = [
    data.aws_iam_policy.read_only_policy.arn,
    data.aws_iam_policy.security_hub_read_only_policy.arn,
    data.aws_iam_policy.support_access_policy.arn
  ]
}
