resource "aws_prometheus_workspace" "prometheus" {
  alias = "${var.environment}-prometheus"
}


resource "random_password" "prometheus_username" {
  length  = 16
  special = false
}
resource "random_password" "prometheus_password" {
  length  = 16
  special = false
}
