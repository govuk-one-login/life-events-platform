locals {
  alarm_prefix = "${var.environment}-gdx"
  queues = {
    supplier-events = "Supplier events queue"
    acquirer-events = "Acquirer events queue"
  }
}

resource "aws_cloudwatch_metric_alarm" "queue_process_error_rate" {
  for_each = local.queues

  alarm_name          = "${local.alarm_prefix}-${each.key}-queue-process-error-rate"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  threshold           = "10"
  alarm_description   = "${each.value} processor error rate"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.sns.topic_arn]
  ok_actions          = [module.sns.topic_arn]

  metric_query {
    id          = "error_rate"
    expression  = "IF(messages_received > 10, 100*errors/messages_received, 0)"
    label       = "Error Rate"
    return_data = "true"
  }

  metric_query {
    id = "errors"
    metric {
      metric_name = "NumberOfMessagesReceived"
      namespace   = "AWS/SQS"
      period      = "300"
      stat        = "Sum"

      dimensions = {
        QueueName = "${var.environment}-gdx-data-share-${each.key}-dlq"
      }
    }
  }

  metric_query {
    id = "messages_received"
    metric {
      metric_name = "NumberOfMessagesReceived"
      namespace   = "AWS/SQS"
      period      = "300"
      stat        = "Sum"

      dimensions = {
        QueueName = "${var.environment}-gdx-data-share-${each.key}"
      }
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "queue_process_error_number" {
  for_each = local.queues

  alarm_name          = "${local.alarm_prefix}-${each.key}-queue-process-error-number"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  threshold           = "1"
  alarm_description   = "Events added to ${each.value} dead letter queue"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.sns.topic_arn]
  ok_actions          = [module.sns.topic_arn]

  metric_name = "NumberOfMessagesReceived"
  namespace   = "AWS/SQS"
  period      = "300"
  statistic   = "Sum"

  dimensions = {
    QueueName = "${var.environment}-gdx-data-share-${each.key}-dlq"
  }
}

resource "aws_cloudwatch_metric_alarm" "cloudfront_5xx_rate_alarm" {
  provider            = aws.us-east-1
  alarm_name          = "${local.alarm_prefix}-cloudfront-5xx-rate"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  threshold           = "10"
  alarm_description   = "Application Cloudfront distribution 5xx rate exceeds 10%"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [module.sns.topic_arn]
  ok_actions          = [module.sns.topic_arn]

  metric_name = "5xxErrorRate"
  namespace   = "AWS/CloudFront"
  period      = "300"
  statistic   = "Average"
  unit        = "Percent"

  dimensions = {
    DistributionId = aws_cloudfront_distribution.gdx_data_share_poc.id
  }
}
