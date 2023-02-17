variable "alarm_name" {
  type = string
}

variable "alarm_description" {
  type = string
}

variable "error_metric" {
  type = object({
    name       = string,
    namespace  = string,
    dimensions = optional(map(string)),
  })
}

variable "success_metric" {
  type = object({
    name       = string,
    namespace  = string,
    dimensions = optional(map(string)),
  })
}

variable "alarm_action" {
  type = string
}
