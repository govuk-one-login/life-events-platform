resource "aws_cloudwatch_dashboard" "dashboard" {
  dashboard_name = var.dashboard_name
  dashboard_body = jsonencode({
    widgets : [for widget in var.widgets :
      {
        type : "metric",
        properties : {
          metrics : [for metric in widget.metrics : [
            var.metric_namespace,
            metric
          ]],
          period : widget.period,
          region : var.region,
          title : widget.title,
          stat : widget.stat,
        }
      }
    ]
  })
}