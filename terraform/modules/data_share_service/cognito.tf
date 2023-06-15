module "cognito" {
  source       = "../cognito"
  environment  = var.environment
  region       = var.region
  callback_url = "${local.gdx_api_base_url}/swagger-ui/oauth2-redirect.html"
  ip_allowlist = var.admin_login_allowed_ip_blocks
}
