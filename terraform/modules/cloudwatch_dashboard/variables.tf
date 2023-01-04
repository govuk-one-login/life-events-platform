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
    title   = string,
    metrics = list(string),
    period  = number,
    stat    = string,
  }))
}