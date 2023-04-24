data "aws_iam_policy_document" "ecs_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "grafana-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json
}

data "aws_iam_policy" "ecs_task_execution" {
  name = "AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = data.aws_iam_policy.ecs_task_execution.arn
}

resource "aws_iam_role" "ecs_task" {
  name               = "grafana-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json
}

data "aws_iam_policy_document" "ecs_task_cloudwatch_logging_access" {
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

resource "aws_iam_policy" "ecs_task_cloudwatch_logging_access" {
  name   = "grafana-ecs-task-cloudwatch-logging-access-policy"
  policy = data.aws_iam_policy_document.ecs_task_cloudwatch_logging_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_cloudwatch_logging_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_cloudwatch_logging_access.arn
}

data "aws_iam_policy_document" "ecs_efs_access" {
  statement {
    actions = [
      "elasticfilesystem:ClientRootAccess",
    ]
    resources = [aws_efs_file_system.grafana.arn]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "ecs_efs_access" {
  name   = "grafana-ecs-efs-access-policy"
  policy = data.aws_iam_policy_document.ecs_efs_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_efs_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_efs_access.arn
}

data "aws_iam_policy_document" "query_aws_policy" {
  statement {
    sid    = "Allow access to read any resource for EC2 Cloudwatch Logs Tags and Xray"
    effect = "Allow"
    actions = [
      "ec2:DescribeTags",
      "ec2:DescribeInstances",
      "ec2:DescribeRegions",

      "cloudwatch:ListMetrics",
      "cloudwatch:GetMetricData",
      "cloudwatch:DescribeAlarmsForMetric",

      "logs:StopQuery",
      "logs:GetQueryResults",

      "tag:GetResources",

      "xray:BatchGetTraces",
      "xray:GetTraceSummaries",
      "xray:GetTraceGraph",
      "xray:GetGroups",
      "xray:GetTimeSeriesServiceStatistics",
      "xray:GetInsightSummaries",
      "xray:GetInsight",
      "xray:GetServiceGraph",
    ]
    resources = ["*"]
  }
  statement {
    sid    = "Allow access to reading alarms"
    effect = "Allow"
    actions = [
      "cloudwatch:DescribeAlarms",
      "cloudwatch:DescribeAlarmHistory",
      "cloudwatch:GetInsightRuleReport",
    ]
    resources = [
      "arn:aws:cloudwatch:*:${var.account_id}:alarm:*",
      "arn:aws:cloudwatch:*:${var.account_id}:insight-rule/*",
    ]
  }
  statement {
    sid    = "Allow access to reading logs and querying"
    effect = "Allow"
    actions = [
      "logs:GetLogGroupFields",
      "logs:DescribeLogGroups",
      "logs:GetLogEvents",
      "logs:StartQuery",
    ]
    resources = [
      "arn:aws:logs:*:${var.account_id}:log-group:*",
    ]
  }
}

resource "aws_iam_policy" "query_aws_policy" {
  name   = "grafana-query-aws-policy"
  policy = data.aws_iam_policy_document.query_aws_policy.json
}

resource "aws_iam_role_policy_attachment" "query_aws_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.query_aws_policy.arn
}
