locals {
  alarm_prefix        = "${var.environment}-gdx"
  client_query_prefix = "SUM(SEARCH('\"${local.metric_namespace}\" MetricName=\"http.client.requests.count\" uri="
  server_query_prefix = "SUM(SEARCH('\"${local.metric_namespace}\" MetricName=\"http.server.requests.count\" uri="

  query_suffix = "', 'Sum', ${local.metric_period}))"


  alarms = {
    lev_error_rate = {
      alarm_name           = "${local.alarm_prefix}-lev-error-rate",
      alarm_description    = "LEV error rate",
      error_metric_query   = "${local.client_query_prefix}\"/v1/registration/death/{id}\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.client_query_prefix}\"/v1/registration/death/{id}\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
    prisoner_search_error_rate = {
      alarm_name           = "${local.alarm_prefix}-prisoner-search-error-rate",
      alarm_description    = "Prisoner search error rate",
      error_metric_query   = "${local.client_query_prefix}\"/prisoner/{id}\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.client_query_prefix}\"/prisoner/{id}\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
    get_event_error_rate = {
      alarm_name           = "${local.alarm_prefix}-get-event-error-rate",
      alarm_description    = "Get event error rate",
      error_metric_query   = "${local.server_query_prefix}\"/events/{id}\" method=\"GET\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.server_query_prefix}\"/events/{id}\" method=\"GET\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
    get_events_error_rate = {
      alarm_name           = "${local.alarm_prefix}-get-events-error-rate",
      alarm_description    = "Get events error rate",
      error_metric_query   = "${local.server_query_prefix}\"/events\" method=\"GET\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.server_query_prefix}\"/events\" method=\"GET\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
    delete_event_error_rate = {
      alarm_name           = "${local.alarm_prefix}-delete-event-error-rate",
      alarm_description    = "Delete event error rate",
      error_metric_query   = "${local.server_query_prefix}\"/events/{id}\" method=\"DELETE\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.server_query_prefix}\"/events/{id}\" method=\"DELETE\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
    publish_event_error_rate = {
      alarm_name           = "${local.alarm_prefix}-publish-event-error-rate",
      alarm_description    = "Publish event error rate",
      error_metric_query   = "${local.server_query_prefix}\"/events\" method=\"POST\" NOT outcome=\"SUCCESS\"${local.query_suffix}"
      success_metric_query = "${local.server_query_prefix}\"/events\" method=\"POST\" outcome=\"SUCCESS\"${local.query_suffix}"
    },
  }
}

module "error_rate_alarms" {
  source   = "../alarm"
  for_each = local.alarms

  alarm_name           = each.value.alarm_name
  alarm_description    = each.value.alarm_description
  error_metric_query   = each.value.error_metric_query
  success_metric_query = each.value.success_metric_query
  alarm_action         = aws_sns_topic.sns_alarm_topic.arn
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

