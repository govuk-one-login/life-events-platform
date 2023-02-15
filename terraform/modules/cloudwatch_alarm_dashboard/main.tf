resource "aws_cloudwatch_dashboard" "alarm_dashboard" {
  dashboard_name = var.dashboard_name
  dashboard_body = jsonencode({
    widgets : [
      for widget in var.widgets :
      {
        type : "metric",
        properties : {
          annotations : {
            alarms : widget.alarms
          }
          region : var.region,
          title : widget.title,
        }
      }
    ]
  })
}

