resource "aws_cloudwatch_log_group" "cloudtrail" {
  name              = "${var.environment}-cloudtrail"
  retention_in_days = var.cloudwatch_retention_period
  kms_key_id        = aws_kms_key.cloudtrail.arn
}

data "aws_iam_policy_document" "cloudtrail_cloudwatch_logs" {
  statement {
    sid = "WriteCloudWatchLogs"

    effect = "Allow"

    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]

    resources = ["${aws_cloudwatch_log_group.cloudtrail.arn}:${var.account_id}_CloudTrail_${var.region}*"]
  }
}

resource "aws_iam_policy" "cloudtrail_cloudwatch_logs" {
  name   = "${var.environment}-cloudtrail-cloudwatch"
  policy = data.aws_iam_policy_document.cloudtrail_cloudwatch_logs.json
}

resource "aws_iam_role_policy_attachment" "cloudtrail_cloudwatch" {
  role       = aws_iam_role.cloudtrail_role.name
  policy_arn = aws_iam_policy.cloudtrail_cloudwatch_logs.arn
}

module "metric_alarms" {
  source = "../cloudtrail_cloudwatch_alarm"

  log_group_name = aws_cloudwatch_log_group.cloudtrail.name
  filter_pattern = "{$.userIdentity.type=\"Root\" && $.userIdentity.invokedBy NOT EXISTS && $.eventType !=\"AwsServiceEvent\"}"
  name           = "root-user-usage"
  sns_topic_arn  = var.sns_topic_arn
}
