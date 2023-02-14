locals {
  notification_emails = []
  prefix              = "${var.environment}-gdx-data-share"
}

resource "aws_cloudformation_stack" "sns_alarm_topic" {
  name = "${local.prefix}-alarms-topic"

  template_body = jsonencode(
    {
      "AWSTemplateFormatVersion" : "2010-09-09",
      "Resources" : {
        "EmailSNSTopic" : {
          "Type" : "AWS::SNS::Topic",
          "Properties" : {
            "DisplayName" : "Softwire - ${local.prefix}-alarms",
            "Subscription" : [
              for address in local.notification_emails : {
                Endpoint = address
                Protocol = "email"
              }
            ]
          }
        }
      },
      "Outputs" : {
        "ARN" : {
          "Description" : "Email SNS Topic ARN",
          "Value" : { "Ref" : "EmailSNSTopic" }
        }
      }
    }
  )
}
