module "deploy_hook" {
  source                      = "../deploy_hook"
  environment                 = var.environment
  region                      = var.region
  cloudwatch_retention_period = var.cloudwatch_retention_period
  codedeploy_arn              = aws_codedeploy_app.gdx_data_share_poc.arn

  gdx_url       = local.gdx_api_base_url
  auth_url      = module.cognito.token_auth_url
  client_id     = module.cognito.deploy_hook_client_id
  client_secret = module.cognito.deploy_hook_client_secret
}
