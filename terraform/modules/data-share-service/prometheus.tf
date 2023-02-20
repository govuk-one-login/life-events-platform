resource "aws_prometheus_workspace" "prometheus" {
  alias = "${var.environment}-prometheus"
}
