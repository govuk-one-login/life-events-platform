module "example_consumer" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_consume}"]
  name         = "example-consumer"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "example_publisher" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_publish}"]
  name         = "example-publisher"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "gro_ingestion" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_publish}"]
  name         = "gro-ingestion"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "deploy_hook" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_publish}", "${local.identifier}/${local.scope_consume}"]
  name         = "deploy-hook"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "len_mock" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_publish}"]
  name         = "len-mock"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "dwp" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scopes       = ["${local.identifier}/${local.scope_consume}"]
  name         = "dwp"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}
