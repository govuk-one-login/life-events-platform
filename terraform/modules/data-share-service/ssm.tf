resource "aws_ssm_parameter" "lev_api_client_name" {
  name  = "${var.environment}-lev-api-client-name"
  type  = "String"
  value = "gdx-data-share"
}

resource "aws_ssm_parameter" "lev_api_client_user" {
  name  = "${var.environment}-lev-api-client-user"
  type  = "SecureString"
  value = "gdx-data-share-user"
}
