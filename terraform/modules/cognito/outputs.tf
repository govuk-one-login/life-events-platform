output "auth_domain" {
  value = "${aws_cognito_user_pool.pool.domain}.auth.${var.region}.amazoncognito.com"
}

output "issuer_domain" {
  value = aws_cognito_user_pool.pool.endpoint
}

output "legacy_inbound_client_id" {
  value = module.legacy_inbound_adaptor.client_id
}

output "legacy_inbound_client_secret" {
  value = module.legacy_inbound_adaptor.client_secret
}

output "legacy_outbound_client_id" {
  value = module.legacy_outbound_adaptor.client_id
}

output "legacy_outbound_client_secret" {
  value = module.legacy_outbound_adaptor.client_secret
}

output "len_client_id" {
  value = module.len_mock.client_id
}

output "len_client_secret" {
  value = module.len_mock.client_secret
}
