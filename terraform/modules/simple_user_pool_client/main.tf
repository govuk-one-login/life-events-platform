// This must be kept in line with createBaseUserPoolClientRequest in CognitoService
resource "aws_cognito_user_pool_client" "client" {
  name                                 = "${var.environment}-${var.name}"
  user_pool_id                         = var.user_pool_id
  allowed_oauth_flows                  = ["client_credentials"]
  allowed_oauth_scopes                 = var.scopes
  allowed_oauth_flows_user_pool_client = true
  access_token_validity                = 60
  id_token_validity                    = 60
  generate_secret                      = true
  explicit_auth_flows                  = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  prevent_user_existence_errors        = "ENABLED"
  enable_token_revocation              = false
  token_validity_units {
    access_token  = "minutes"
    id_token      = "minutes"
    refresh_token = "days"
  }
}
