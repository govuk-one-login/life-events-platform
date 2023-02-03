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

variable "prisoner-event-enabled" {}
variable "prisoner-search-url" {}
variable "hmpps-auth_url" {}
variable "prisoner-search-client-id" {}
variable "prisoner-search-client-secret" {}
variable "prisoner-event-queue-name" {}
variable "prisoner-event-dlq-name" {}
