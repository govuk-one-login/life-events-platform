output "gdx_url" {
  value = "https://${aws_cloudfront_distribution.gdx_data_share_poc.domain_name}"
}
output "token_auth_url" {
  value = "https://${module.cognito.auth_domain}/oauth2/token"
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
