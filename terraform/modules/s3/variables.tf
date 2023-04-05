variable "environment" {}
variable "name" {}
variable "expiration_days" {
  type    = number
  default = null
}
variable "kms_key_policy_json" {
  type    = string
  default = null
}
variable "allow_logs" {
  type    = bool
  default = false
}
