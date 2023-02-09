module "deploy_hook" {
  source                      = "../deploy_hook"
  environment                 = var.environment
  region                      = var.region
  cloudwatch_retention_period = var.cloudwatch_retention_period
  codedeploy_arn              = aws_codedeploy_deployment_group.gdx_data_share_poc.arn

  test_gdx_url     = "${local.gdx_api_base_url}:8443"
  test_auth_header = random_password.test_auth_header.result
  auth_url         = module.cognito.token_auth_url
  client_id        = module.cognito.deploy_hook_client_id
  client_secret    = module.cognito.deploy_hook_client_secret
}
