resource "aws_cloudwatch_event_rule" "tunnel_notification" {
  name        = "${var.environment}-bastion-tunnel-notification"
  description = "Send an SNS notification when a tunnel is initiated to the bastion host"

  event_pattern = jsonencode({
    "source" : ["aws.ssm"],
    "detail-type" : ["AWS API Call via CloudTrail"],
    "detail" : {
      "eventSource" : ["ssm.amazonaws.com"],
      "eventName" : ["StartSession"]
    }
  })
}

resource "aws_cloudwatch_event_target" "tunnel_notification" {
  rule      = aws_cloudwatch_event_rule.tunnel_notification.name
  target_id = "SendToSNS"
  arn       = module.sns.topic_arn
}

resource "aws_cloudwatch_event_rule" "admin_role_notification" {
  name        = "${var.environment}-admin-role-notification"
  description = "Send an SNS notification when a user assumes an admin role"

  event_pattern = jsonencode({
    "source" : ["aws.sts"],
    "detail-type" : ["AWS API Call via CloudTrail"],
    "detail" : {
      "eventSource" : ["sts.amazonaws.com"],
      "eventName" : ["AssumeRole"],
      "requestParameters" : {
        "roleArn" : [
          {
            "suffix" : "-admin"
          }
        ]
      }
    }
  })
}

resource "aws_cloudwatch_event_rule" "admin_role_notification_us_east_1" {
  provider = aws.us-east-1

  name        = "${var.environment}-admin-role-notification"
  description = "Send an SNS notification when a user assumes an admin role"

  event_pattern = jsonencode({
    "source" : ["aws.sts"],
    "detail-type" : ["AWS API Call via CloudTrail"],
    "detail" : {
      "eventSource" : ["sts.amazonaws.com"],
      "eventName" : ["AssumeRole"],
      "requestParameters" : {
        "roleArn" : [
          {
            "suffix" : "-admin"
          }
        ]
      }
    }
  })
}

resource "aws_cloudwatch_event_rule" "admin_role_notification_eu_west_1" {
  provider = aws.eu-west-1

  name        = "${var.environment}-admin-role-notification"
  description = "Send an SNS notification when a user assumes an admin role"

  event_pattern = jsonencode({
    "source" : ["aws.sts"],
    "detail-type" : ["AWS API Call via CloudTrail"],
    "detail" : {
      "eventSource" : ["sts.amazonaws.com"],
      "eventName" : ["AssumeRole"],
      "requestParameters" : {
        "roleArn" : [
          {
            "suffix" : "-admin"
          }
        ]
      }
    }
  })
}

resource "aws_cloudwatch_event_target" "admin_role_notification" {
  rule      = aws_cloudwatch_event_rule.admin_role_notification.name
  target_id = "SendToSNS"
  arn       = module.sns.topic_arn
}

resource "aws_cloudwatch_event_target" "admin_role_notification_us_east_1" {
  provider = aws.us-east-1

  rule      = aws_cloudwatch_event_rule.admin_role_notification_us_east_1.name
  target_id = "SendToSNS"
  arn       = module.sns-us-east-1.topic_arn
}

resource "aws_cloudwatch_event_target" "admin_role_notification_eu_west_1" {
  provider = aws.eu-west-1

  rule      = aws_cloudwatch_event_rule.admin_role_notification_eu_west_1.name
  target_id = "SendToSNS"
  arn       = module.sns-eu-west-1.topic_arn
}

