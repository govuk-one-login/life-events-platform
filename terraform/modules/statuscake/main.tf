data "aws_ssm_parameter" "statuscake_api_key" {
  name = aws_ssm_parameter.statuscake_api_key.name
}

provider "statuscake" {
  api_token = data.aws_ssm_parameter.statuscake_api_key.value
}

resource "statuscake_contact_group" "gdx_platform_team" {
  name = "GDX Platform"

  email_addresses = [
    "gdx-dev-team@digital.cabinet-office.gov.uk"
  ]
}

resource "statuscake_uptime_check" "uptime_check" {
  for_each = var.env_url_pair

  name           = each.key
  check_interval = 30
  confirmation   = 1
  trigger_rate   = 0
  regions        = ["london", "dublin"]

  contact_groups = [
    statuscake_contact_group.gdx_platform_team.id
  ]

  http_check {
    enable_cookies   = true
    follow_redirects = true
    timeout          = 20
    validate_ssl     = true
    request_method   = "HTTP"
    status_codes = [
      "204", "205", "206", "303", "400", "401", "403", "404", "405", "406", "408", "410", "413", "444", "429", "494",
      "495", "496", "499", "500", "501", "502", "503", "504", "505", "506", "507", "508", "509", "510", "511", "521",
      "522", "523", "524", "520", "598", "599"
    ]
  }

  monitored_resource {
    address = each.value
  }
}
