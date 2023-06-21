terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.4"
      configuration_aliases = [aws.us-east-1]
    }
  }
}

data "aws_canonical_user_id" "current" {}
