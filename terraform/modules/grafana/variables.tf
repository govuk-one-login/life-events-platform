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

variable "sns_topic_arn" {
  type = string
}
