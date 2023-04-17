variable "account_id" {
  type = string
}
variable "region" {
  type = string
}
variable "prefix" {
  type    = string
  default = ""
}
variable "name" {
  type = string
}
variable "suffix" {
  type    = string
  default = ""
}
variable "expiration_days" {
  type    = number
  default = null
}
variable "tiering_noncurrent_days" {
  type    = number
  default = null
}

variable "add_log_bucket" {
  type    = bool
  default = true
}

variable "use_kms" {
  type    = bool
  default = true
}

variable "sns_arn" {
  type = string
}

variable "cross_account_arns" {
  type    = list(string)
  default = []
}
