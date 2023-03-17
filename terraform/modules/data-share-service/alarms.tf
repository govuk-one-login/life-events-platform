locals {
  alarm_prefix = "${var.environment}-gdx"
}

resource "aws_cloudwatch_metric_alarm" "queue_process_error_rate" {
  alarm_name          = "${local.alarm_prefix}-queue-process-error-rate"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  threshold           = "10"
  alarm_description   = "Queue processor error rate"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.sns_alarm_topic.arn]
  ok_actions          = [aws_sns_topic.sns_alarm_topic.arn]

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
        QueueName = "${var.environment}-gdx-data-share-poc-data-processor-queue-dlq"
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
        QueueName = "${var.environment}-gdx-data-share-poc-data-processor-queue"
      }
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "queue_process_error_number" {
  alarm_name          = "${local.alarm_prefix}-queue-process-error-number"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  threshold           = "1"
  alarm_description   = "Events added to dead letter queue"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.sns_alarm_topic.arn]
  ok_actions          = [aws_sns_topic.sns_alarm_topic.arn]

  metric_name = "NumberOfMessagesReceived"
  namespace   = "AWS/SQS"
  period      = "300"
  statistic   = "Sum"

  dimensions = {
    QueueName = "${var.environment}-gdx-data-share-poc-data-processor-queue-dlq"
  }
}
