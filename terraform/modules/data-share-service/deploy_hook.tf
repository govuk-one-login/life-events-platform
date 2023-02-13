module "deploy_hook" {
  source                      = "../deploy_hook"
  environment                 = var.environment
  region                      = var.region
  cloudwatch_retention_period = var.cloudwatch_retention_period
  codedeploy_arn              = aws_codedeploy_deployment_group.gdx_data_share_poc.arn

  security_group_id = aws_security_group.lambda.id
  subnet_ids        = module.vpc.private_subnet_ids

  test_gdx_url     = "http://${aws_lb.load_balancer.dns_name}:8080"
  test_auth_header = random_password.test_auth_header.result
  auth_url         = module.cognito.token_auth_url
  client_id        = module.cognito.deploy_hook_client_id
  client_secret    = module.cognito.deploy_hook_client_secret
}

resource "aws_security_group" "lambda" {
  name        = "${var.environment}-deploy-hook-lambda"
  description = "Egress rules for lambda"
  vpc_id      = module.vpc.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group_rule" "lambda_https" {
  type              = "egress"
  protocol          = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "Lambda security group egress rule for HTTPS"
  security_group_id = aws_security_group.lambda.id
}

#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group_rule" "lambda_test" {
  type              = "egress"
  protocol          = "tcp"
  from_port         = 8080
  to_port           = 8080
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "Lambda security group egress rule for connecting to test GDX port"
  security_group_id = aws_security_group.lambda.id
}
