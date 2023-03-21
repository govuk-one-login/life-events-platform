variable "account_id" {
  type = string
}
variable "environments" {
  type = list(string)
}
variable "terraform_lock_table_name" {
  type = string
}
variable "cross_account_bucket" {
  type    = string
  default = ""
}
