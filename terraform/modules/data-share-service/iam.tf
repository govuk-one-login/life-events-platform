locals {
  ecs_task_sqs_access_json = sensitive(data.aws_iam_policy_document.ecs_task_sqs_access.json)
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${var.environment}-task-execution-role"
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

resource "aws_iam_role_policy_attachment" "ecs_task_execution_policy" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = data.aws_iam_policy.ecs_task_execution.arn
}

data "aws_iam_policy_document" "ecs_task_execution_ssm" {
  statement {
    actions = [
      "ssm:GetParameters",
      "secretsmanager:GetSecretValue",
      "kms:Decrypt"
    ]
    effect    = "Allow"
    resources = local.ecs_task_execution_parameters.*.arn
  }
}

resource "aws_iam_policy" "ecs_task_execution_ssm" {
  name   = "${var.environment}-ecs-task-execution-ssm"
  policy = data.aws_iam_policy_document.ecs_task_execution_ssm.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_ssm" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = aws_iam_policy.ecs_task_execution_ssm.arn
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
    resources = [aws_cloudwatch_log_group.ecs_logs.arn, aws_cloudwatch_log_group.ecs_adot_logs.arn]
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

data "aws_iam_policy" "ecs_task_prometheus_access" {
  name = "AmazonPrometheusRemoteWriteAccess"
}

resource "aws_iam_role_policy_attachment" "ecs_task_prometheus_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = data.aws_iam_policy.ecs_task_prometheus_access.arn
}

data "aws_ssm_parameter" "prisoner_event_aws_account_id" {
  name = aws_ssm_parameter.prisoner_event_aws_account_id.name
}

data "aws_ssm_parameter" "prisoner_event_queue_name" {
  name = aws_ssm_parameter.prisoner_event_queue_name.name
}

data "aws_ssm_parameter" "prisoner_event_dlq_name" {
  name = aws_ssm_parameter.prisoner_event_dlq_name.name
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
      module.supplier_event_queue.queue_arn,
      module.supplier_event_queue.dead_letter_queue_arn,
      "arn:aws:sqs:eu-west-2:${data.aws_ssm_parameter.prisoner_event_aws_account_id.value}:${data.aws_ssm_parameter.prisoner_event_queue_name.value}",
      "arn:aws:sqs:eu-west-2:${data.aws_ssm_parameter.prisoner_event_aws_account_id.value}:${data.aws_ssm_parameter.prisoner_event_dlq_name.value}",
    ]
    effect = "Allow"
  }

  statement {
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [
      module.supplier_event_queue.queue_kms_key_arn,
      module.supplier_event_queue.dead_letter_queue_kms_key_arn,
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_sqs_access" {
  name   = "${var.environment}-ecs-task-sqs-access"
  policy = local.ecs_task_sqs_access_json
}

resource "aws_iam_role_policy_attachment" "ecs_task_sqs_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_sqs_access.arn
}

data "aws_iam_policy_document" "ecs_task_rds_access" {
  statement {
    actions = ["rds-db:connect"]
    resources = [
      "arn:aws:rds-db:${var.region}:${data.aws_caller_identity.current.account_id}:dbuser:${aws_rds_cluster.rds_postgres_cluster.cluster_resource_id}/${var.db_username}"
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_rds_access" {
  name   = "${var.environment}-ecs-task-rds-access-policy"
  policy = data.aws_iam_policy_document.ecs_task_rds_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_rds_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_rds_access.arn
}

data "aws_iam_policy_document" "ecs_task_cognito_access" {
  statement {
    actions = ["cognito-idp:CreateUserPoolClient"]
    resources = [
      module.cognito.user_pool_arn
    ]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "ecs_task_cognito_access" {
  name   = "${var.environment}-ecs_task_cognito_access-policy"
  policy = data.aws_iam_policy_document.ecs_task_cognito_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_cognito_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_cognito_access.arn
}

resource "aws_iam_role_policy_attachment" "ecs_task_xray_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}
