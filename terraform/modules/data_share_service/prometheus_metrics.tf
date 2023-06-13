locals {
  rate5m_metrics = [
    for k, v in local.http_requests :
    <<EOF
    - record: http_requests:${k}:rate5m
      expr: sum(rate(http_server_requests_seconds_count{uri="${v.uri}", method="${v.method}"}[5m]))
EOF
  ]
  rate5m_1w_average_metrics = [
    for k, v in local.http_requests :
    <<EOF
    - record: http_requests:${k}:rate5m:avg_over_time_1w
      expr: avg_over_time(http_requests:${k}:rate5m[1w])
EOF
  ]
  rate5m_1w_st_dev_metrics = [
    for k, v in local.http_requests :
    <<EOF
    - record: http_requests:${k}:rate5m:stddev_over_time_1w
      expr: stddev_over_time(http_requests:${k}:rate5m[1w])
EOF
  ]
  rate5m_prediction = [
    for k, v in local.http_requests :
    <<EOF
    - record: http_requests:${k}:rate5m_prediction
      expr: >
       quantile(0.5,
         label_replace(
           avg_over_time(http_requests:${k}:rate5m[4h] offset 166h)
           + http_requests:${k}:rate5m:avg_over_time_1w - http_requests:${k}:rate5m:avg_over_time_1w offset 1w
           , "offset", "1w", "", "")
         or
         label_replace(
           avg_over_time(http_requests:${k}:rate5m[4h] offset 334h)
           + http_requests:${k}:rate5m:avg_over_time_1w - http_requests:${k}:rate5m:avg_over_time_1w offset 2w
           , "offset", "2w", "", "")
         or
         label_replace(
           avg_over_time(http_requests:${k}:rate5m[4h] offset 502h)
           + http_requests:${k}:rate5m:avg_over_time_1w - http_requests:${k}:rate5m:avg_over_time_1w offset 3w
           , "offset", "3w", "", "")
       )
       without (offset)
EOF
  ]

  unconsumed_events_max = [
    <<EOF
    - record: unconsumed_events_max
      expr: max by(acquirer, acquirer_subscription_id) (unconsumed_events)
EOF
  ]

  metrics = concat(
    local.rate5m_metrics,
    local.rate5m_1w_average_metrics,
    local.rate5m_1w_st_dev_metrics,
    local.rate5m_prediction,
    local.unconsumed_events_max
  )

  metric_rules = join("\n", local.metrics)
}
