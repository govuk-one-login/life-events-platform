output "gdx_url" {
  value = local.gdx_api_base_url
}
output "token_auth_url" {
  value = module.cognito.token_auth_url
}
output "len_client_id" {
  value = module.cognito.len_client_id
}
output "len_client_secret" {
  value = module.cognito.len_client_secret
}
output "consumer_client_id" {
  value = module.cognito.consumer_client_id
}
output "consumer_client_secret" {
  value = module.cognito.consumer_client_secret
}
