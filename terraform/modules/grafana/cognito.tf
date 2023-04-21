resource "aws_cognito_user_pool" "pool" {
  name = "grafana"

  mfa_configuration = "ON"

  password_policy {
    minimum_length                   = 12
    require_lowercase                = true
    require_uppercase                = true
    require_numbers                  = true
    require_symbols                  = true
    temporary_password_validity_days = 7
  }

  software_token_mfa_configuration {
    enabled = true
  }

  admin_create_user_config {
    allow_admin_create_user_only = true

    invite_message_template {
      email_subject = "Your temporary password for GDX Grafana"
      email_message = "Your username is {username} and temporary password is {####}. \n Please go to https://${var.hosted_zone_name}/login/generic_oauth to login and click the `Sign in with Cognito` button."
      sms_message   = "Your username is {username} and temporary password is {####}."
    }
  }

  verification_message_template {
    default_email_option  = "CONFIRM_WITH_LINK"
    email_subject_by_link = "Verify access to GDX Grafana"
    email_message_by_link = "Click the link below to verify access to Grafana \n {##Click Here##}"
  }

  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  auto_verified_attributes = ["email"]
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "grafana"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_user_pool_client" "grafana" {
  name                                 = "grafana"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["email", "openid", "profile", "aws.cognito.signin.user.admin"]
  generate_secret                      = true
  explicit_auth_flows                  = ["ALLOW_REFRESH_TOKEN_AUTH", "ALLOW_USER_PASSWORD_AUTH", "ALLOW_ADMIN_USER_PASSWORD_AUTH"]
  callback_urls                        = ["https://${var.hosted_zone_name}/login/generic_oauth"]
  logout_urls                          = ["https://${var.hosted_zone_name}/login"]
  supported_identity_providers         = ["COGNITO"]
}

resource "aws_cognito_user_group" "main" {
  name         = "Admin"
  user_pool_id = aws_cognito_user_pool.pool.id
}
