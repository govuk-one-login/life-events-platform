output "auth_domain" {
  value = "${aws_cognito_user_pool.pool.domain}.auth.${var.region}.amazoncognito.com"
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
