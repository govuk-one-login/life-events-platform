locals {
  identifier    = "events"
  scope_publish = "publish"
  scope_consume = "consume"
  scope_admin   = "admin"
}

resource "aws_cognito_user_pool" "pool" {
  name                = "${var.environment}-gdx-data-share"
  deletion_protection = "ACTIVE"

  mfa_configuration = "ON"

  password_policy {
    minimum_length                   = 12
    require_lowercase                = true
    require_uppercase                = true
    require_numbers                  = true
    require_symbols                  = true
    temporary_password_validity_days = 7
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "admin_only"
      priority = 1
    }
  }


  software_token_mfa_configuration {
    enabled = true
  }

  admin_create_user_config {
    allow_admin_create_user_only = true

    invite_message_template {
      email_subject = "Your temporary password for DI Event Platform"
      email_message = "Your username is {username} and temporary password is {####}."
      sms_message   = "Your username is {username} and temporary password is {####}."
    }
  }

}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "${var.environment}-gdx-data-share"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_resource_server" "events" {
  identifier = local.identifier
  name       = "GDX Events"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = local.scope_publish
    scope_description = "Can publish events with GDX"
  }

  scope {
    scope_name        = local.scope_consume
    scope_description = "Can consume events from GDX"
  }

  scope {
    scope_name        = local.scope_admin
    scope_description = "Can manage events and users of GDX"
  }
}

resource "aws_cognito_user_pool_client" "admin" {
  name                                 = "${var.environment}-admin"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_scopes                 = aws_cognito_resource_server.events.scope_identifiers
  generate_secret                      = true
  explicit_auth_flows                  = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                        = [var.callback_url]
  supported_identity_providers         = ["COGNITO"]
}
