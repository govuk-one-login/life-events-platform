variable "region" {
  type = string
}
variable "environment" {
  type = string
}
variable "account_id" {
  type = string
}
variable "rules" {
  type = list(object({ rule = string, disabled_reason = string }))
}
variable "s3_event_notification_sns_topic_arn" {
  type = string
}
