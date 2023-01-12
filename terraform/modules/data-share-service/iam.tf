resource "aws_iam_role" "ecs_execution" {
  name               = "${var.environment}-execution-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy" "ecs_task_execution" {
  name = "AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ecs_execution_policy" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = data.aws_iam_policy.ecs_task_execution.arn
}

resource "aws_iam_role" "ecs_task" {
  name               = "${var.environment}-task-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "ecs_task" {
  statement {
    actions = [
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel"
    ]
    resources = ["*"]
    effect    = "Allow"
  }

  statement {
    actions = [
      "cloudwatch:PutMetricData"
    ]
    resources = ["*"]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task" {
  name   = "${var.environment}-ecs-task-policy"
  policy = data.aws_iam_policy_document.ecs_task.json
}

resource "aws_iam_role_policy_attachment" "ecs_task" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task.arn
}

data "aws_iam_policy_document" "ecs_task_cloudwatch_access" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogStreams"
    ]
    resources = [aws_cloudwatch_log_group.ecs_logs.arn]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_cloudwatch_access" {
  name   = "${var.environment}-ecs-task-cloudwatch-access-policy"
  policy = data.aws_iam_policy_document.ecs_task_cloudwatch_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_cloudwatch_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_cloudwatch_access.arn
}

data "aws_iam_policy_document" "ecs_task_s3_access" {
  statement {
    actions = [
      "s3:ListBucket",
      "s3:GetObject",
      "s3:GetObjectTagging",
      "s3:PutObject",
      "s3:PutObjectTagging",
      "s3:ReplicateObject",
      "s3:DeleteObject"
    ]
    resources = [
      module.ingress.arn,
      module.ingress.objects_arn,
      module.ingress_archive.arn,
      module.ingress_archive.objects_arn,
      module.egress.arn,
      module.egress.objects_arn,
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_s3_access" {
  name   = "${var.environment}-ecs-task-s3-access"
  policy = data.aws_iam_policy_document.ecs_task_s3_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_s3_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_s3_access.arn
}

data "aws_iam_policy_document" "ecs_task_s3_key" {
  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [
      module.ingress.kms_arn,
      module.ingress_archive.kms_arn,
      module.egress.kms_arn,
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_s3_key" {
  name   = "${var.environment}-ecs-task-s3-key"
  policy = data.aws_iam_policy_document.ecs_task_s3_key.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_s3_key" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_s3_key.arn
}

data "aws_iam_policy_document" "ecs_task_sqs_access" {
  statement {
    actions = [
      "sqs:DeleteMessage",
      "sqs:ReceiveMessage",
      "sqs:SendMessage",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility",
      "sqs:GetQueueAttributes",
    ]
    resources = [
      module.data_processor_queue.queue_arn,
      module.data_processor_queue.dead_letter_queue_arn,
      module.audit_queue.queue_arn,
      module.audit_queue.dead_letter_queue_arn,
      module.other_department_queue.queue_arn,
      module.other_department_queue.dead_letter_queue_arn,
      module.outbound_adaptor_queue.queue_arn,
      module.outbound_adaptor_queue.dead_letter_queue_arn
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [
      module.data_processor_queue.queue_kms_key_arn,
      module.data_processor_queue.dead_letter_queue_kms_key_arn,
      module.audit_queue.queue_kms_key_arn,
      module.audit_queue.dead_letter_queue_kms_key_arn,
      module.other_department_queue.queue_kms_key_arn,
      module.other_department_queue.dead_letter_queue_kms_key_arn,
      module.outbound_adaptor_queue.queue_kms_key_arn,
      module.outbound_adaptor_queue.dead_letter_queue_kms_key_arn
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_sqs_access" {
  name   = "${var.environment}-ecs-task-sqs-access"
  policy = data.aws_iam_policy_document.ecs_task_sqs_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_sqs_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_sqs_access.arn
}

data "aws_iam_policy_document" "ecs_task_sns_access" {
  statement {
    actions = [
      "sns:Publish",
      "sns:Subscribe",
      "sns:Unsubscribe",
      "sns:GetTopicAttributes",
    ]
    resources = [
      module.sns.sns_topic_arn
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [
      module.sns.sns_kms_key_arn
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_sns_access" {
  name   = "${var.environment}-ecs-task-sns-access"
  policy = data.aws_iam_policy_document.ecs_task_sns_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_sns_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_sns_access.arn
}
