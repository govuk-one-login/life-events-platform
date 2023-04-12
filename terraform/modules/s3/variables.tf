variable "environment" {}
variable "name" {}
variable "expiration_days" {
  type    = number
  default = null
}

variable "allow_logs" {
  type    = bool
  default = false
}
variable "account_id" {
  type    = string
  default = ""
}
variable "region" {
  type    = string
  default = ""
}
