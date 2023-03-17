variable "security_rules" {
  type = list(object({ rule = string, disabled_reason = string }))
}
