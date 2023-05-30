terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 4.0"
      configuration_aliases = [aws.us-east-1, aws.eu-west-1]
    }
  }
}

module "assume_admin_role_alert" {
  source = "../assume_admin_role_alert"

  sns_arn = var.eu_west_2_sns_arn
}

module "assume_admin_role_alert_us_east_1" {
  source = "../assume_admin_role_alert"
  providers = {
    aws = aws.us-east-1
  }

  sns_arn = var.us_east_1_sns_arn
}

module "assume_admin_role_alert_eu_west_1" {
  source = "../assume_admin_role_alert"
  providers = {
    aws = aws.eu-west-1
  }

  sns_arn = var.eu_west_1_sns_arn
}
