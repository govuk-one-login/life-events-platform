module "deploy_hook" {
  source                      = "../deploy_hook"
  environment                 = var.environment
  region                      = var.region
  cloudwatch_retention_period = var.cloudwatch_retention_period
  codedeploy_arn              = aws_codedeploy_app.gdx_data_share_poc.arn

  security_group_id = aws_security_group.lb_test.id
  subnet_ids        = module.vpc.public_subnet_ids

  test_gdx_url  = "http://${aws_lb.load_balancer.dns_name}:8080"
  auth_url      = module.cognito.token_auth_url
  client_id     = module.cognito.deploy_hook_client_id
  client_secret = module.cognito.deploy_hook_client_secret
}
