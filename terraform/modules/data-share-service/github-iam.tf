module "github_iam" {
  source      = "../github_env_iam"
  environment = var.environment
  account_id  = data.aws_caller_identity.current.account_id
}
