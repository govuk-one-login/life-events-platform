locals {
  alarm_prefix = "${var.environment}-gdx"
  alarms = {
    lev_error_rate = {
      alarm_name        = "${local.alarm_prefix}-lev-error-rate",
      alarm_description = "LEV error rate",
      error_metric = {
        name      = "API_RESPONSES.ErrorsFromLev",
        namespace = local.metric_namespace,
      },
      success_metric = {
        name      = "API_RESPONSES.ResponsesFromLev",
        namespace = local.metric_namespace,
      },
    },
    prisoner_search_error_rate = {
      alarm_name        = "${local.alarm_prefix}-prisoner-search-error-rate",
      alarm_description = "Prisoner search error rate",
      error_metric = {
        name      = "API_RESPONSES.ErrorsFromPrisonerSearch",
        namespace = local.metric_namespace,
      },
      success_metric = {
        name      = "API_RESPONSES.ResponsesFromPrisonerSearch",
        namespace = local.metric_namespace,
      },
    },
    get_event_error_rate = {
      alarm_name        = "${local.alarm_prefix}-get-event-error-rate",
      alarm_description = "Get event error rate",
      error_metric = {
        name      = "API_CALLS.GetEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = false
        }
      },
      success_metric = {
        name      = "API_CALLS.GetEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = true
        }
      },
    },
    get_events_error_rate = {
      alarm_name        = "${local.alarm_prefix}-get-events-error-rate",
      alarm_description = "Get events error rate",
      error_metric = {
        name      = "API_CALLS.GetEvents",
        namespace = local.metric_namespace,
        dimensions = {
          success = false
        }
      },
      success_metric = {
        name      = "API_CALLS.GetEvents",
        namespace = local.metric_namespace,
        dimensions = {
          success = true
        }
      },
    },
    delete_event_error_rate = {
      alarm_name        = "${local.alarm_prefix}-delete-event-error-rate",
      alarm_description = "Delete event error rate",
      error_metric = {
        name      = "API_CALLS.DeleteEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = false
        }
      },
      success_metric = {
        name      = "API_CALLS.DeleteEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = true
        }
      },
    },
    publish_event_error_rate = {
      alarm_name        = "${local.alarm_prefix}-publish-event-error-rate",
      alarm_description = "Publish event error rate",
      error_metric = {
        name      = "API_CALLS.PublishEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = false
        }
      },
      success_metric = {
        name      = "API_CALLS.PublishEvent",
        namespace = local.metric_namespace,
        dimensions = {
          success = true
        }
      },
    },
  }
}

module "error_rate_alarms" {
  source   = "../alarm"
  for_each = local.alarms

  alarm_name        = each.value.alarm_name
  alarm_description = each.value.alarm_description
  error_metric      = each.value.error_metric
  success_metric    = each.value.success_metric
  alarm_action      = aws_sns_topic.sns_alarm_topic.arn
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

resource "aws_cloudwatch_metric_alarm" "unconsumed_events" {
  alarm_name          = "${local.alarm_prefix}-growing-unconsumed-events"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  threshold           = "100000"
  alarm_description   = "Number of unconsumed events"
  treat_missing_data  = "notBreaching"
  alarm_actions       = [aws_sns_topic.sns_alarm_topic.arn]
  ok_actions          = [aws_sns_topic.sns_alarm_topic.arn]

  metric_name = "UnconsumedEvents.value"
  namespace   = local.metric_namespace
  period      = "300"
  statistic   = "Sum"
}
