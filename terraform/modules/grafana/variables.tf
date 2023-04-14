variable "region" {
  type = string
}
variable "account_id" {
  type = string
}

variable "vpc_id" {
  type = string
}
variable "public_subnet_ids" {
  type = list(string)
}
variable "private_subnet_ids" {
  type = list(string)
}
variable "vpc_cidr" {
  type = string
}

variable "ecr_url" {
  type = string
}

variable "s3_event_notification_sns_topic_arn" {
  type = string
}
