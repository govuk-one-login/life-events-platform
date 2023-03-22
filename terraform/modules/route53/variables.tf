variable "hosted_zone_name" {
  type = string
}
variable "subdomains" {
  type = list(object({
    name         = string
    name_servers = list(string)
  }))
  default = []
}
