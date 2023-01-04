resource "aws_cognito_user_pool" "pool" {
  name = "${var.environment}-gdx-data-share"
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "${var.environment}-gdx-data-share"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_resource_server" "events" {
  identifier = "events"
  name       = "GDX Events"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "publish"
    scope_description = "Can publish events with GDX"
  }

  scope {
    scope_name        = "consume"
    scope_description = "Can consume events from GDX"
  }

  scope {
    scope_name        = "admin"
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

resource "aws_cognito_user_pool_client" "events_publish" {
  name                                 = "${var.environment}-events-publish"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_scopes                 = [aws_cognito_resource_server.events.scope_identifiers[0]]
  generate_secret                      = true
  explicit_auth_flows                  = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                        = [var.callback_url]
  supported_identity_providers         = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "events_consume" {
  name                                 = "${var.environment}-events-consume"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_scopes                 = [aws_cognito_resource_server.events.scope_identifiers[1]]
  generate_secret                      = true
  explicit_auth_flows                  = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                        = [var.callback_url]
  supported_identity_providers         = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "legacy_inbound_adapter" {
  name                 = "${var.environment}-legacy-inbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = [aws_cognito_resource_server.events.scope_identifiers[0]]
  generate_secret      = true
}

resource "aws_cognito_user_pool_client" "legacy_outbound_adapter" {
  name                 = "${var.environment}-legacy-outbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = [aws_cognito_resource_server.events.scope_identifiers[1]]
  generate_secret      = true
}
