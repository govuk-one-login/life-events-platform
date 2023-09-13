data "aws_ssm_parameter" "statuscake_api_key" {
  name = aws_ssm_parameter.statuscake_api_key.name
}

provider "statuscake" {
  api_token = data.aws_ssm_parameter.statuscake_api_key.value
}

