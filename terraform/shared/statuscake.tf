data "aws_ssm_parameter" "statuscake_api_key" {
  name = aws_ssm_parameter.statuscake_api_key.name
}

#provider "statuscake" {
#  api_token = data.aws_ssm_parameter.statuscake_api_key.value
#}
#
#module "statuscake" {
#  source      = "../modules/statuscake"
#  env_url_pair =  {
#    "dev" = "https://dev.share-life-events.service.gov.uk/health/ping"
#    "demo" = "https://demo.share-life-events.service.gov.uk/health/ping"
#    }
#}
