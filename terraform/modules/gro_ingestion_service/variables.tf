variable "region" {
  type = string
}
variable "account_id" {
  type = string
}
variable "environment" {
  type = string
}

variable "gdx_url" {
  type = string
}
variable "auth_url" {
  type = string
}
variable "publisher_client_id" {
  type = string
}
variable "publisher_client_secret" {
  type = string
}

variable "cloudwatch_retention_period" {
  type    = number
  default = 365
}
