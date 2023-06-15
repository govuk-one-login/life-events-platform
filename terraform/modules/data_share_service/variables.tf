variable "environment" {
  type = string
}
variable "region" {
  type = string
}
variable "ecr_url" {
  type = string
}
variable "cloudwatch_retention_period" {
  type = number
}
variable "vpc_cidr" {
  type = string
}
variable "lev_url" {
  type = string
}
variable "db_username" {
  type = string
}
variable "grafana_task_role_name" {
  type = string
}
variable "hosted_zone_id" {
  type = string
}
variable "hosted_zone_name" {
  type = string
}
variable "delete_event_function_arn" {
  type = string
}
variable "delete_event_function_name" {
  type = string
}
variable "enrich_event_function_arn" {
  type = string
}
variable "enrich_event_function_name" {
  type = string
}
variable "admin_alerts_enabled" {
  type    = bool
  default = true
}
variable "database_tunnel_alerts_enabled" {
  type    = bool
  default = true
}
variable "admin_login_allowed_ip_blocks" {
  type        = list(string)
  description = "List of IP blocks in CIDR notation to permit admin login. Null disables IP restriction"
}
