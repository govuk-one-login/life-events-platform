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
  dashboard_name = "metrics-dashboard"
  dashboard_body = jsonencode({
    "widgets" : [
      {
        "type" : "metric",
        "properties" : {
          "metrics" : [
            [
              "gdxApp",
              "API_CALLS.IngestedEvents.count"
            ],
            [
              ".",
              "API_CALLS.CallsToLev.count"
            ],
            [
              ".",
              "API_CALLS.ResponsesFromLev.count"
            ],
            [
              ".",
              "API_CALLS.CallsToHmrc.count"
            ],
            [
              ".",
              "API_CALLS.ResponsesFromHmrc.count"
            ],
            [
              ".",
              "API_CALLS.CallsToPoll.count"
            ],
            [
              ".",
              "API_CALLS.CallsToEnrich.count"
            ]
          ],
          "period" : 300,
          "region" : var.region,
          "title" : "API calls",
          "stat": "SUM3"
        }
      }
    ]
  })

}