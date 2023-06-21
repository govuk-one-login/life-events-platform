terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.4"
    }
  }
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "lev_api_ecr_role_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["build.apprunner.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

#tfsec:ignore:aws-ec2-no-public-ingress-sgr
#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group" "lev_api" {
  name_prefix = "${var.environment}-lev-api-"
  description = "For LEV API and DB, access only on port 5432"

  ingress {
    protocol    = "tcp"
    from_port   = 5432
    to_port     = 5432
    cidr_blocks = ["0.0.0.0/0"]
    description = "For LEV API and DB, access only on port 5432"
  }

  egress {
    protocol    = "tcp"
    from_port   = 5432
    to_port     = 5432
    cidr_blocks = ["0.0.0.0/0"]
    description = "For LEV API and DB, access only on port 5432"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_role" "lev_api_ecr_role" {
  name = "${var.environment}-lev-api-ecr-role"

  assume_role_policy = data.aws_iam_policy_document.lev_api_ecr_role_assume_policy.json
}

data "aws_iam_policy" "apprunner_ecr_policy" {
  name = "AWSAppRunnerServicePolicyForECRAccess"
}

resource "aws_iam_role_policy_attachment" "lev_api_ecr_role" {
  role       = aws_iam_role.lev_api_ecr_role.name
  policy_arn = data.aws_iam_policy.apprunner_ecr_policy.arn
}

resource "aws_apprunner_service" "lev_api" {
  service_name = "${var.environment}-lev-api"

  source_configuration {
    image_repository {
      image_configuration {
        port = "8000"
        runtime_environment_variables = {
          LISTEN_HOST       = "0.0.0.0"
          LISTEN_PORT       = "8000"
          POSTGRES_USER     = random_string.rds_username.result
          POSTGRES_PASSWORD = random_password.rds_password.result
          POSTGRES_HOST     = aws_rds_cluster.lev_rds_postgres_cluster.endpoint
          POSTGRES_DB       = aws_rds_cluster.lev_rds_postgres_cluster.database_name
          POSTGRES_SSL      = "true"
        }
      }
      image_identifier      = "${var.ecr_url}/quay/ukhomeofficedigital/lev-api:latest"
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

  depends_on = [aws_iam_role_policy_attachment.lev_api_ecr_role]
}
