variable "account_id" {
  type = string
}

variable "config_role_arn" {
  type = string
}
variable "config_s3_id" {
  type = string
}
variable "config_s3_kms_arn" {
  type = string
}
variable "rules" {
  type = list(object({ rule = string, disabled_reason = string }))
}

