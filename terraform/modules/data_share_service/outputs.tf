output "gdx_url" {
  value = local.gdx_api_base_url
}
output "base_auth_url" {
  value = module.cognito.base_auth_url
}
output "token_auth_url" {
  value = module.cognito.token_auth_url
}
output "len_client_id" {
  value     = module.cognito.len_client_id
  sensitive = true
}
output "len_client_secret" {
  value     = module.cognito.len_client_secret
  sensitive = true
}
output "consumer_client_id" {
  value     = module.cognito.consumer_client_id
  sensitive = true
}
output "consumer_client_secret" {
  value     = module.cognito.consumer_client_secret
  sensitive = true
}

output "gro_ingestion_client_id" {
  value     = module.cognito.gro_ingestion_client_id
  sensitive = true
}
output "gro_ingestion_client_secret" {
  value     = module.cognito.gro_ingestion_client_secret
  sensitive = true
}
