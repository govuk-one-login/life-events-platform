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

data "aws_iam_policy_document" "xray_access" {
  statement {
    actions = [
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
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "xray_access" {
  name   = "grafana-ecs-xray-access-policy"
  policy = data.aws_iam_policy_document.xray_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_xray_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.xray_access.arn
}

data "aws_iam_policy_document" "cloudwatch_access" {
  statement {
    sid = "AllowReadingMetricsFromCloudWatchNeedAnyResource"
    actions = [
      "cloudwatch:ListMetrics",
    ]
    resources = ["*"]
    effect    = "Allow"
  }
  statement {
    sid = "AllowReadingMetricsFromCloudWatch"
    actions = [
      "cloudwatch:DescribeAlarms",
      "cloudwatch:DescribeAlarmHistory",
      "cloudwatch:GetInsightRuleReport",
      "cloudwatch:GetMetricData",
      "cloudwatch:DescribeAlarmsForMetric",
    ]
    resources = [
      "arn:aws:cloudwatch:${var.region}:${var.account_id}:alarm:*",
      "arn:aws:cloudwatch:${var.region}:${var.account_id}:insight-rule/*",
    ]
    effect = "Allow"
  }
  statement {
    sid = "AllowReadingLogsFromCloudWatch"
    actions = [
      "logs:GetLogGroupFields",
      "logs:DescribeLogGroups",
      "logs:GetLogEvents",
      "logs:StartQuery",
      "logs:StopQuery",
      "logs:GetQueryResults"
    ]
    resources = [
      "arn:aws:logs:${var.region}:${var.account_id}:log-group:*",
      "arn:aws:logs:${var.region}:${var.account_id}:log-group:*:log-stream:*"
    ]
    effect = "Allow"
  }
  statement {
    sid = "AllowReadingLogsFromCloudWatchSensitive"
    actions = [
    ]
    resources = ["arn:aws:logs:${var.region}:${var.account_id}:*"]
    effect    = "Allow"
  }
  statement {
    sid = "AllowReadingTagsInstancesRegionsFromEC2"
    actions = [
      "ec2:DescribeTags",
      "ec2:DescribeInstances",
      "ec2:DescribeRegions"
    ]
    resources = ["*"]
    effect    = "Allow"
  }
  statement {
    sid = "AllowReadingResourcesForTags"
    actions = [
      "tag:GetResources"
    ]
    resources = ["*"]
    effect    = "Allow"
  }
}

resource "aws_iam_policy" "cloudwatch_access" {
  name   = "grafana-ecs-cloudwatch-access-policy"
  policy = data.aws_iam_policy_document.cloudwatch_access.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_cloudwatch_access" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.cloudwatch_access.arn
}
