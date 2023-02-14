variable "metric_namespace" {
  type = string
}
variable "region" {
  type = string
}
variable "dashboard_name" {
  type = string
}
variable "widgets" {
  type = list(object({
    title  = string,
    alarms = list(string),
  }))
}
