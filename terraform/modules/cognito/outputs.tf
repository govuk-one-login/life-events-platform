locals {
  auth_domain = "${aws_cognito_user_pool.pool.domain}.auth.${var.region}.amazoncognito.com"
}

output "auth_domain" {
  value = local.auth_domain
}

output "token_auth_url" {
  value = "https://${local.auth_domain}/oauth2/token"
}

output "issuer_domain" {
  value = aws_cognito_user_pool.pool.endpoint
}

output "user_pool_id" {
  value = aws_cognito_user_pool.pool.id
}

output "user_pool_arn" {
  value = aws_cognito_user_pool.pool.arn
}

output "acquirer_scope" {
  value = "${local.identifier}/${local.scope_consume}"
}

output "supplier_scope" {
  value = "${local.identifier}/${local.scope_publish}"
}

output "admin_scope" {
  value = "${local.identifier}/${local.scope_admin}"
}

output "len_client_id" {
  value = module.len_mock.client_id
}

output "len_client_secret" {
  value = module.len_mock.client_secret
}

output "consumer_client_id" {
  value = module.example_consumer.client_id
}

output "consumer_client_secret" {
  value = module.example_consumer.client_secret
}

output "gro_ingestion_client_id" {
  value = module.gro_ingestion.client_id
}

output "gro_ingestion_client_secret" {
  value = module.gro_ingestion.client_secret
}

output "deploy_hook_client_id" {
  value = module.deploy_hook.client_id
}

output "deploy_hook_client_secret" {
  value = module.deploy_hook.client_secret
}
