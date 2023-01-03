locals {
  metric_namespace = "${var.environment}-gdx"
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "${var.environment}-gdx-data-share-poc-ecs-logs"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_cloudwatch_log_group" "lb_sg_update" {
  name              = "/aws/lambda/${aws_lambda_function.lb_sg_update.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.log_key.arn
}

resource "aws_cloudwatch_dashboard" "metrics_dashboard" {
  dashboard_name = "${var.environment}-metrics-dashboard"
  dashboard_body = jsonencode({
    "widgets" : [
      {
        "type" : "metric",
        "properties" : {
          "metrics" : [
            [
              local.metric_namespace,
              "API_CALLS.IngestedEvents.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.CallsToLev.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.ResponsesFromLev.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.CallsToHmrc.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.ResponsesFromHmrc.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.CallsToPoll.count"
            ],
            [
              local.metric_namespace,
              "API_CALLS.CallsToEnrich.count"
            ]
          ],
          "period" : 300,
          "region" : var.region,
          "title" : "API calls",
          "stat" : "Sum"
        }
      }
    ]
  })

}