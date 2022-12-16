module "cognito" {
  source       = "../cognito"
  environment  = var.environment
  region       = var.region
  callback_url = "https://${aws_cloudfront_distribution.gdx_data_share_poc.domain_name}/webjars/swagger-ui/oauth2-redirect.html"
}