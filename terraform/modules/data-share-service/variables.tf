variable "environment" {}
variable "region" {}
variable "ecr_url" {}
variable "cloudwatch_retention_period" {}
variable "vpc_cidr" {}
variable "lev_url" {}

variable "db_username" {}
variable "externally_allowed_cidrs" {
  type = list(string)
}

variable "prisoner_event_enabled" {}
variable "prisoner_search_url" {}
variable "hmpps_auth_url" {}
variable "prisoner_search_client_id" {}
variable "prisoner_search_client_secret" {}
variable "prisoner_event_queue_name" {}
variable "prisoner_event_dlq_name" {}
