variable "environment" {
  type = string
}
variable "account_id" {
  type = string
}
variable "region" {
  type = string
}

variable "name_prefix" {
  type = string
}
variable "vpc_cidr" {
  type = string
}
variable "sns_topic_arn" {
  type = string
}
variable "acl_ingress_private" {
  type = list(any)
}
variable "acl_egress_private" {
  type = list(any)
}
variable "acl_ingress_public" {
  type = list(any)
}
variable "acl_egress_public" {
  type = list(any)
}
