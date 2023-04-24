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
variable "allow_cloudtrail_logs" {
  type    = bool
  default = false
}
variable "allow_delivery_logs" {
  type    = bool
  default = false
}
variable "allow_lb_logs" {
  type    = bool
  default = false
}
variable "allow_config_logs" {
  type    = bool
  default = false
}

variable "use_kms" {
  type    = bool
  default = true
}

variable "sns_arn" {
  type = string
}

variable "object_writer_owner" {
  type    = bool
  default = false
}

variable "cross_account_arns" {
  type    = list(string)
  default = []
}

variable "allow_versioning" {
  type    = bool
  default = true
}

variable "notify_bucket_deletions" {
  type    = bool
  default = true
}
