module "consumer_queue" {
  source = "../sqs"
  environment = var.environment
  queue_name = "${var.environment}-example-consumer-queue"
}

data "aws_iam_policy_document" "lambda_sqs_access" {
  statement {
    actions = [
      "sqs:SendMessage",
      "sqs:GetQueueUrl",
      "sqs:GetQueueAttributes",
    ]
    resources = [
      module.consumer_queue.queue_arn,
      module.consumer_queue.dead_letter_queue_arn,
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [
      module.consumer_queue.queue_kms_key_arn,
      module.consumer_queue.dead_letter_queue_kms_key_arn,
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "lambda_sqs_access" {
  name   = "${var.environment}-consumer-lambda-sqs-access"
  policy = data.aws_iam_policy_document.lambda_sqs_access.json
}

resource "aws_iam_role_policy_attachment" "lambda_sqs_access" {
  role       = aws_iam_role.get_event.name
  policy_arn = aws_iam_policy.lambda_sqs_access.arn
}
