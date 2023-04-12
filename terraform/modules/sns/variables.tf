variable "account_id" {
  type = string
}
variable "environment" {
  type = string
}
variable "name" {
  type = string
}
variable "notification_emails" {
  type = list(string)
}
variable "prometheus_arn" {
  type    = string
  default = null
}
