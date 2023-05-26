variable "account_id" {
  type = string
}
variable "environment" {
  type = string
}
variable "region" {
  type = string
}
variable "name" {
  type = string
}
variable "notification_emails" {
  type = list(string)
}

variable "arns_which_can_publish" {
  type    = list(string)
  default = []
}
variable "allow_s3_notification" {
  type    = bool
  default = true
}
variable "allow_codestar_notification" {
  type    = bool
  default = true
}
variable "allow_eventbridge_notification" {
  type    = bool
  default = true
}
