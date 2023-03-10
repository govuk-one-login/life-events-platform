locals {
  anomaly_alerts = [
    for k, v in local.http_requests :
    <<EOF
    - alert: Anomalous traffic for endpoint ${k}
      expr: >
       abs(
         (
           http_requests:${k}:rate5m - http_requests:${k}:rate5m_prediction
         ) / http_requests:${k}:rate5m:stddev_over_time_1w
       ) > 2
      for: 5m
      annotations:
        summary: Absolute z score is greater than 2 based on seasonal predictions for endpoint ${k}
EOF
  ]

  events_alerts = [
    <<EOF
    - alert: Growing Unconsumed Events
      expr: max(UnconsumedEvents) > 500000
      for: 5m
      annotations:
        summary: Over 500000 unconsumed events in database
EOF
  ]
  alerts = concat(local.anomaly_alerts, local.events_alerts)

  alert_rules = join("\n", local.alerts)
}
