module "legacy_inbound_adaptor" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scope        = "${local.identifier}/${local.scope_publish}"
  name         = "legacy-inbound-adapter"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "legacy_outbound_adaptor" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scope        = "${local.identifier}/${local.scope_consume}"
  name         = "legacy-outbound-adapter"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "len_mock" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scope        = "${local.identifier}/${local.scope_publish}"
  name         = "len-mock"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "dwp" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scope        = "${local.identifier}/${local.scope_consume}"
  name         = "dwp"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}

module "example_consumer" {
  source       = "../simple_user_pool_client"
  environment  = var.environment
  scope        = "${local.identifier}/${local.scope_consume}"
  name         = "example-consumer"
  user_pool_id = aws_cognito_user_pool.pool.id

  depends_on = [aws_cognito_resource_server.events]
}
