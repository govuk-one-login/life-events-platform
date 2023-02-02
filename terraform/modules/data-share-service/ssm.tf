resource "aws_ssm_parameter" "lev_api_client_name" {
  name  = "lev_api_client_name"
  type  = "String"
  value = "gdx-data-share"
}

resource "aws_ssm_parameter" "lev_api_client_user" {
  name  = "lev_api_client_user"
  type  = "SecureString"
  value = "gdx-data-share-user"
}
