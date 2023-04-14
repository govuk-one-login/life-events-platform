data "aws_ssm_parameter" "statuscake_api_key" {
  name = aws_ssm_parameter.statuscake_api_key.name
}

#module "statuscake" {
#  source      = "../modules/statuscake"
#  statuscake_api_key = data.aws_ssm_parameter.statuscake_api_key.value
#  env_url_pair =  {
#    "dev" = "https://dev.share-life-events.service.gov.uk/health/ping"
#    "demo" = "https://demo.share-life-events.service.gov.uk/health/ping"
#    }
#}
