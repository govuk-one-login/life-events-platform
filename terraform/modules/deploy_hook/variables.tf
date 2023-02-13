variable "environment" {}
variable "region" {}
variable "cloudwatch_retention_period" {}
variable "codedeploy_arn" {}

variable "security_group_id" {}
variable "subnet_ids" {
  type = list(string)
}

variable "test_gdx_url" {}
variable "test_auth_header" {
  sensitive = true
}
variable "auth_url" {}
variable "client_id" {}
variable "client_secret" {}
