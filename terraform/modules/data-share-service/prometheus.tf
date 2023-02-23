resource "aws_prometheus_workspace" "prometheus" {
  alias = "${var.environment}-prometheus"

  logging_configuration {
    log_group_arn = "${aws_cloudwatch_log_group.prometheus_logs.arn}:*"
  }
}

resource "random_password" "prometheus_username" {
  length  = 16
  special = false
}
resource "random_password" "prometheus_password" {
  length  = 16
  special = false
}

data "aws_iam_policy_document" "grafana_prometheus_access" {
  statement {
    actions = [
      "aps:GetLabels",
      "aps:GetMetricMetadata",
      "aps:GetSeries",
      "aps:QueryMetrics",
      "aps:ListRules",
      "aps:ListAlertManagerSilences",
      "aps:ListAlertManagerAlerts",
      "aps:GetAlertManagerStatus",
      "aps:ListAlertManagerAlertGroups",
      "aps:PutAlertManagerSilences",
      "aps:DeleteAlertManagerSilence",
    ]
    resources = [aws_prometheus_workspace.prometheus.arn]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "grafana_prometheus_access" {
  name   = "${var.environment}-grafana-prometheus-access"
  policy = data.aws_iam_policy_document.grafana_prometheus_access.json
}

resource "aws_iam_role_policy_attachment" "grafana_prometheus_access" {
  role       = var.grafana_task_role_name
  policy_arn = aws_iam_policy.grafana_prometheus_access.arn
}

resource "aws_prometheus_alert_manager_definition" "prometheus" {
  workspace_id = aws_prometheus_workspace.prometheus.id
  definition   = <<EOF
alertmanager_config: |
  route:
    receiver: 'default'
  receivers:
    - name: 'default'
      sns_configs:
      - topic_arn: ${aws_sns_topic.sns_alarm_topic.arn}
        sigv4:
          region: eu-west-2
EOF
}

resource "aws_prometheus_rule_group_namespace" "test" {
  name         = "test"
  workspace_id = aws_prometheus_workspace.prometheus.id
  data         = <<EOF
groups:
  - name: Events
    rules:
    - alert: Growing Unconsumed Events
      expr: sum(UnconsumedEvents) > 500
      for: 5m
      annotations:
        summary: Over 500 unconsumed events in database
EOF
}
