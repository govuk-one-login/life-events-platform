variable "environment" {}
variable "region" {}
variable "callback_url" {}
variable "ip_allowlist" {
  default     = null
  type        = list(string)
  description = "List of IP blocks in CIDR notation to form an allowlist for access to the cognito user pool. Null disables IP restriction"
}
