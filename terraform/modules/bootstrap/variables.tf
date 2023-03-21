variable "s3_bucket_name" {
  type = string
}
variable "dynamo_db_table_name" {
  type = string
}

variable "cross_account_arns" {
  type    = list(string)
  default = []
}
