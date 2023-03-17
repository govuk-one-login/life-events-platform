variable "region" {}
variable "account_id" {}
variable "rules" {
  type = list(object({ rule = string, disabled_reason = string }))
}
