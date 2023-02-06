module "deploy_hook" {
  source                      = "../deploy_hook"
  environment                 = var.environment
  region                      = var.region
  cloudwatch_retention_period = var.cloudwatch_retention_period

  gdx_url       = local.gdx_url
  auth_url      = module.cognito.token_auth_url
  client_id     = module.cognito.deploy_hook_client_id
  client_secret = module.cognito.deploy_hook_client_secret
}
