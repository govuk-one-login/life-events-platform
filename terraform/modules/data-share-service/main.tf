terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 4.0"
      configuration_aliases = [aws.us-east-1, aws.eu-west-1]
    }
  }
}

data "aws_caller_identity" "current" {}
