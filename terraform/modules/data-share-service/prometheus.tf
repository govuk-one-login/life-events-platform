locals {
  http_requests = {
    post_event = {
      method = "POST",
      uri    = "/events"
    },
    get_event = {
      method = "GET",
      uri    = "/events/{id}"
    },
    get_events = {
      method = "GET",
      uri    = "/events"
    },
    delete_event = {
      method = "DELETE",
      uri    = "/events/{id}"
    }
  }
}

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

resource "aws_prometheus_rule_group_namespace" "request_metrics" {
  for_each     = local.http_requests
  name         = "${var.environment}/requests"
  workspace_id = aws_prometheus_workspace.prometheus.id
  data         = <<EOF
groups:
  - name: ${each.key}
    rules:
    - record: ${each.key}:http_requests:rate5m
      expr: sum(rate(http_server_requests_seconds_count{uri=${each.value.uri}, method=${each.value.method}[5m]))
    - record: ${each.key}:http_requests:rate5m:avg_over_time_1w
      expr: avg_over_time(${each.key}:http_requests:rate5m[1w])
    - record: ${each.key}:http_requests:rate5m:stddev_over_time_1w
      expr: stddev_over_time(${each.key}:http_requests:rate5m[1w])
    - record: ${each.key}:http_requests:rate5m_prediction
      expr: >
       quantile(0.5,
         label_replace(
           avg_over_time(${each.key}:http_requests:rate5m[4h] offset 166h)
           + ${each.key}:http_requests:rate5m:avg_over_time_1w - job:http_requests:rate5m:avg_over_time_1w offset 1w
           , "offset", "1w", "", "")
         or
         label_replace(
           avg_over_time(${each.key}:http_requests:rate5m[4h] offset 334h)
           + ${each.key}:http_requests:rate5m:avg_over_time_1w - ${each.key}:http_requests:rate5m:avg_over_time_1w offset 2w
           , "offset", "2w", "", "")
         or
         label_replace(
           avg_over_time(${each.key}:http_requests:rate5m[4h] offset 502h)
           + ${each.key}:http_requests:rate5m:avg_over_time_1w - ${each.key}:http_requests:rate5m:avg_over_time_1w offset 3w
           , "offset", "3w", "", "")
       )
       without (offset)
EOF
}

resource "aws_prometheus_rule_group_namespace" "unconsumed_event_alerts" {
  name         = var.environment
  workspace_id = aws_prometheus_workspace.prometheus.id
  data         = <<EOF
groups:
  - name: alerts
    rules:
    - alert: Growing Unconsumed Events
      expr: max(UnconsumedEvents) > 500000
      for: 5m
      annotations:
        summary: Over 500000 unconsumed events in database
EOF
}

resource "aws_prometheus_rule_group_namespace" "anomalous_traffic_alerts" {
  for_each     = local.http_requests
  name         = var.environment
  workspace_id = aws_prometheus_workspace.prometheus.id
  data         = <<EOF
groups:
  - name: alerts
    rules:
    - alert: Anomalous traffic for endpoint ${each.key}
      expr: >
       abs(
         (
           ${each.key}:http_requests:rate5m - ${each.key}:http_requests:rate5m_prediction
         ) / ${each.key}:http_requests:rate5m:stddev_over_time_1w
       ) > 2
      for: 5m
      annotations:
        summary: Absolute z score is greater than 2 based on seasonal predictions for endpoint ${each.key}
EOF
}
