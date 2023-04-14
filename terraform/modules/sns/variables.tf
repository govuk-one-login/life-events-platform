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

variable "arns_which_can_publish" {
  type    = list(string)
  default = []
}
variable "allow_s3_notification" {
  type    = bool
  default = false
}
