resource "aws_cognito_user_pool" "pool" {
  name = "${var.environment}-gdx-data-share"
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "${var.environment}-gdx-data-share"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_user_pool_client" "management_client" {
  name                         = "${var.environment}-management-client"
  user_pool_id                 = aws_cognito_user_pool.pool.id
  allowed_oauth_flows          = ["implicit"]
  allowed_oauth_scopes         = concat(aws_cognito_resource_server.data_receiver.scope_identifiers, aws_cognito_resource_server.data_retriever.scope_identifiers)
  generate_secret              = true
  explicit_auth_flows          = ["ADMIN_NO_SRP_AUTH"]
  callback_urls                = [var.callback_url]
  supported_identity_providers = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "legacy_inbound_adapter" {
  name                 = "${var.environment}-legacy-inbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = aws_cognito_resource_server.data_receiver.scope_identifiers
  generate_secret      = true
}

resource "aws_cognito_user_pool_client" "legacy_outbound_adapter" {
  name                 = "${var.environment}-legacy-outbound-adapter"
  user_pool_id         = aws_cognito_user_pool.pool.id
  allowed_oauth_flows  = ["client_credentials"]
  allowed_oauth_scopes = aws_cognito_resource_server.data_retriever.scope_identifiers
  generate_secret      = true
}

resource "aws_cognito_resource_server" "data_receiver" {
  identifier = "data_receiver"
  name       = "GDX Data Receiver"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "notify"
    scope_description = "Can notify GDX of events"
  }
}

resource "aws_cognito_resource_server" "data_retriever" {
  identifier = "data_retriever"
  name       = "GDX Data Retriever"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "read"
    scope_description = "Can call back to Data Retriever API to obtain information about event"
  }
}

resource "aws_cognito_resource_server" "events" {
  identifier = "events"
  name       = "Events Poller"

  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "poll"
    scope_description = "Can Poll"
  }
}