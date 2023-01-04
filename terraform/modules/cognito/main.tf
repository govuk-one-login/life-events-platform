resource "aws_cognito_user_pool" "pool" {
  name = "${var.environment}-gdx-data-share"
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "${var.environment}-gdx-data-share"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_user_pool_client" "admin" {
  name                                 = "${var.environment}-admin"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_scopes = concat(
    aws_cognito_resource_server.events_consume.scope_identifiers,
    aws_cognito_resource_server.events_publish.scope_identifiers,
    aws_cognito_resource_server.admin.scope_identifiers,
  )
  generate_secret              = true
  explicit_auth_flows          = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                = [var.callback_url]
  supported_identity_providers = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "events_consume" {
  name                                 = "${var.environment}-events-consume"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["implicit"]
  allowed_oauth_scopes                 = aws_cognito_resource_server.events_consume.scope_identifiers
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
  allowed_oauth_scopes                 = aws_cognito_resource_server.events_publish.scope_identifiers
  generate_secret                      = true
  explicit_auth_flows                  = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                        = [var.callback_url]
  supported_identity_providers         = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "legacy_inbound_adapter" {
  name                 = "${var.environment}-legacy-inbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = aws_cognito_resource_server.events_publish.scope_identifiers
  generate_secret      = true
}

resource "aws_cognito_user_pool_client" "legacy_outbound_adapter" {
  name                 = "${var.environment}-legacy-outbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = aws_cognito_resource_server.events_consume.scope_identifiers
  generate_secret      = true
}

resource "aws_cognito_resource_server" "events_publish" {
  identifier = "events"
  name       = "GDX Events Publish"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "publish"
    scope_description = "Can publish events with GDX"
  }
}

resource "aws_cognito_resource_server" "events_consume" {
  identifier = "events"
  name       = "GDX Events Consume"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "consume"
    scope_description = "Can consumer events from GDX"
  }
}

resource "aws_cognito_resource_server" "admin" {
  identifier = "events"
  name       = "GDX Admin"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "admin"
    scope_description = "Can manage events and users of GDX"
  }
}