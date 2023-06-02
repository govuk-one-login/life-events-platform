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
  type = bool
  default = true
}
