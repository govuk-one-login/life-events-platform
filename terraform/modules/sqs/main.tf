data "aws_caller_identity" "current" {}

# rework as part of #46
#tfsec:ignore:aws-iam-no-user-attached-policies 
resource "aws_iam_user" "sqs_user" {
  name = "${var.queue_name}-sqs-queue"
}

resource "aws_iam_access_key" "sqs_user" {
  user = aws_iam_user.sqs_user.name
}

data "aws_iam_policy_document" "sqs_policy" {
  statement {
    actions = [
      "sqs:DeleteMessage",
      "sqs:ReceiveMessage",
      "sqs:SendMessage"
    ]

    resources = [
      aws_sqs_queue.queue.arn,
      aws_sqs_queue.dead_letter_queue.arn
    ]
  }
}

resource "aws_iam_user_policy" "sqs_policy" {
  name   = "${var.queue_name}-sqs-queue"
  policy = data.aws_iam_policy_document.sqs_policy.json
  user   = aws_iam_user.sqs_user.name
}

resource "aws_kms_key" "sqs_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sqs queue for ${var.queue_name}"
}

resource "aws_kms_alias" "sqs_key_alias" {
  name          = "alias/${var.environment}/sqs-queue-${var.queue_name}"
  target_key_id = aws_kms_key.sqs_key.arn
}

resource "aws_sqs_queue" "queue" {
  name              = var.queue_name
  kms_master_key_id = aws_kms_key.sqs_key.arn
}

resource "aws_kms_key" "dead_letter_queue_kms_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt DLQ for ${var.queue_name}"
}

resource "aws_kms_alias" "dead_letter_queue_kms_key_alias" {
  name          = "alias/${var.environment}/sqs-dead-letter-queue-${var.queue_name}"
  target_key_id = aws_kms_key.sqs_key.arn
}

resource "aws_sqs_queue" "dead_letter_queue" {
  name              = "${var.queue_name}-dlq"
  kms_master_key_id = aws_kms_key.dead_letter_queue_kms_key.arn
}

resource "aws_sqs_queue_redrive_policy" "queue" {
  queue_url = aws_sqs_queue.queue.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead_letter_queue.arn
    maxReceiveCount     = 4
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "dead_letter_queue" {
  queue_url = aws_sqs_queue.dead_letter_queue.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue",
    sourceQueueArns   = [aws_sqs_queue.queue.arn]
  })
}
