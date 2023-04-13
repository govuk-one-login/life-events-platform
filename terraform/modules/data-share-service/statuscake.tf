data "aws_ssm_parameter" "statuscake_api_key" {
  name = aws_ssm_parameter.statuscake_api_key.name
}

provider "statuscake" {
  api_token = data.aws_ssm_parameter.statuscake_api_key.value
}

module "statuscake" {
  source      = "../statuscake"
  environment = var.environment
  ping_url    = var.ping_check_url
}
