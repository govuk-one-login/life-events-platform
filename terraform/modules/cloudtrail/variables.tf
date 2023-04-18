variable "account_id" {
  type = string
}
variable "region" {
  type = string
}
variable "environment" {
  type = string
}
variable "cloudwatch_retention_period" {
  type = number
}

variable "sns_topic_arn" {
  type = string
}
