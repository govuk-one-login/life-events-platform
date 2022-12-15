output "auth_domain" {
  value = aws_cognito_user_pool.pool.domain
}

output "legacy_inbound_client_id" {
  value = aws_cognito_user_pool_client.legacy_inbound_adapter.id
}

output "legacy_inbound_client_secret" {
  value = aws_cognito_user_pool_client.legacy_inbound_adapter.client_secret
}

output "legacy_outbound_client_id" {
  value = aws_cognito_user_pool_client.legacy_outbound_adapter.id
}

output "legacy_outbound_client_secret" {
  value = aws_cognito_user_pool_client.legacy_outbound_adapter.client_secret
}