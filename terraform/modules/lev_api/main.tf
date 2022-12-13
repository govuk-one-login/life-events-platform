terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

data "aws_caller_identity" "current" {}

resource "aws_iam_role" "lev_api_ecr_role" {
  name = "${var.environment_name}-lev-api-ecr-role"

  assume_role_policy = jsonencode({
    Version : "2012-10-17",
    Statement : [
      {
        Action : "sts:AssumeRole",
        Principal : {
          Service : "build.apprunner.amazonaws.com",
        },
        Effect : "Allow",
      }
    ]
  })
}

data "aws_iam_policy" "apprunner_ecr_policy" {
  name = "AWSAppRunnerServicePolicyForECRAccess"
}

resource "aws_iam_role_policy_attachment" "lev_api_ecr_role" {
  role       = aws_iam_role.lev_api_ecr_role.name
  policy_arn = data.aws_iam_policy.apprunner_ecr_policy.arn
}

resource "aws_apprunner_service" "lev_api" {
  service_name = "${var.environment_name}-lev-api"

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
    authentication_configuration {
      access_role_arn = aws_iam_role.lev_api_ecr_role.arn
    }
    auto_deployments_enabled = true
  }

  health_check_configuration {
    path     = "/readiness"
    protocol = "HTTP"
  }

  depends_on = [aws_iam_role.lev_api_ecr_role]
}
