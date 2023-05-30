terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 4.0"
    }
  }
}

resource "aws_cloudwatch_event_rule" "admin_role_notification" {
  name        = "admin-role-notification"
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
  arn       = var.sns_arn
}
