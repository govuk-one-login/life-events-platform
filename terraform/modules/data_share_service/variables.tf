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

variable "prisoner_event_enabled" {
  type = string
}
variable "prisoner_search_url" {
  type = string
}
variable "hmpps_auth_url" {
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

variable "enrich_event_function_name" {
  type = string
}
