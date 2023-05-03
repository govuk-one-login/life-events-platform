variable "admin_users" {
  type = list(string)
}
variable "read_only_users" {
  type = list(string)
}
variable "terraform_lock_table_name" {
  type = string
}
variable "account_id" {
  type = string
}
