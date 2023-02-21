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

data "aws_iam_policy" "grafana_prometheus_access" {
  name = "AmazonPrometheusQueryAccess"
}

resource "aws_iam_role_policy_attachment" "grafana_prometheus_access" {
  role       = var.grafana_task_role_name
  policy_arn = data.aws_iam_policy.grafana_prometheus_access.arn
}
