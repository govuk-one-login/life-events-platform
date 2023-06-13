locals {
  anomaly_alerts = [
    for k, v in local.http_requests :
    <<EOF
    - alert: ${var.environment}  Anomalous traffic for endpoint ${k}
      expr: >
       abs(
         (
           http_requests:${k}:rate5m - http_requests:${k}:rate5m_prediction
         ) / http_requests:${k}:rate5m:stddev_over_time_1w
       ) > 2
      for: 5m
      annotations:
        summary: ${var.environment} Absolute z score is greater than 2 based on seasonal predictions for endpoint ${k}
EOF
  ]

  events_alerts = [
    <<EOF
    - alert: ${var.environment} Growing Unconsumed Events
      expr: max(UnconsumedEvents) > 500000
      for: 5m
      annotations:
        summary: ${var.environment} Over 500000 unconsumed events in database
EOF
  ]

  sqs_alerts = [
    <<EOF
    - alert: ${var.environment} Acquirer SQS DLQ has messages
      expr: max by(queue_name) (dlq_length) > 0
      for: 5m
      annotations:
        summary: ${var.environment} Acquirer SQS DLQ has messages
EOF,
    <<EOF
    - alert: ${var.environment} Acquirer SQS message age over 3 days
      expr: max by(queue_name) (age_of_oldest_message) > 259200
      for: 5m
      annotations:
        summary: ${var.environment} Acquirer SQS message age over 3 days
EOF
  ]

  error_rate_alerts = [
    for k, v in local.http_requests :
    <<EOF
    - alert: ${var.environment} Error rate exceeded 10% for endpoint ${k}
      expr: >
        (
          sum(rate(http_server_requests_seconds_count{uri="${v.uri}", method="${v.method}", outcome!="SUCCESS"}[5m])) /
          sum(rate(http_server_requests_seconds_count{uri="${v.uri}", method="${v.method}"}[5m]))
        ) > 0.1
      for: 5m
      annotations:
        summary: ${var.environment} Error rate exceeded 10% for over 5m for 5m for endpoint ${k}
EOF
  ]

  lev_error_Rate_alert = [
    <<EOF
    - alert: ${var.environment} Error rate exceeded 10% for LEV death records
      expr: >
        (
          sum(rate(http_client_requests_seconds_count{uri="/v1/registration/death/{id}", outcome!="SUCCESS"}[5m])) /
          sum(rate(http_client_requests_seconds_count{uri="/v1/registration/death/{id}"}[5m]))
        ) > 0.1
      for: 5m
      annotations:
        summary: ${var.environment} Error rate exceeded 10% for over 5m for 5m for LEV death records
EOF
  ]

  alerts = concat(local.anomaly_alerts, local.events_alerts, local.error_rate_alerts, local.sqs_alerts)

  alert_rules = join("\n", local.alerts)
}
