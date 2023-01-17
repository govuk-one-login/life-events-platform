resource "aws_sns_topic_subscription" "outbound_subscription" {
  topic_arn     = module.sns.sns_topic_arn
  protocol      = "sqs"
  endpoint      = module.outbound_adaptor_queue.queue_arn
  filter_policy = jsonencode({ "consumer" : ["Internal Adaptor"] })
}

resource "aws_sns_topic_subscription" "other_department_subscription" {
  topic_arn     = module.sns.sns_topic_arn
  protocol      = "sqs"
  endpoint      = module.other_department_queue.queue_arn
  filter_policy = jsonencode({ "consumer" : ["Pub/Sub Consumer"] })
}
