data "aws_caller_identity" "current" {}

resource "aws_apprunner_service" "lev_api" {
  service_name = "lev-api"

  source_configuration {
    image_repository {
      image_configuration {
        port = "8000"
        runtime_environment_variables = {
          LISTEN_HOST = "0.0.0.0"
          LISTEN_PORT = "8000"
          MOCK        = "true"
        }
      }
      image_identifier      = "${data.aws_caller_identity.current.account_id}.dkr.ecr.eu-west-2.amazonaws.com/quay/ukhomeofficedigital/lev-api:latest"
      image_repository_type = "ECR"
    }
    auto_deployments_enabled = true
  }

  health_check_configuration {
    path     = "/readiness"
    protocol = "HTTP"
  }
}
