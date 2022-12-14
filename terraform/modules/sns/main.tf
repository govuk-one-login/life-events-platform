data "aws_caller_identity" "current" {}

resource "aws_sns_topic" "topic" {
  name_prefix       = "${var.environment}-gdx-sns-topic"
  display_name      = var.topic_display_name
  kms_master_key_id = aws_kms_key.sns_key.arn
}

# rework as part of #46
#tfsec:ignore:aws-iam-no-user-attached-policies 
resource "aws_iam_user" "sns_user" {
  name = "${var.environment}-sns-topic-${var.topic_display_name}"
}

resource "aws_iam_access_key" "sns_user" {
  user = aws_iam_user.sns_user.name
}

data "aws_iam_policy_document" "sns_policy" {
  statement {
    actions = [
      "sns:Publish",
      "sns:Subscribe",
      "sns:Unsubscribe",
    ]

    resources = [
      aws_sns_topic.topic.arn
    ]
  }
}

resource "aws_iam_user_policy" "sns_policy" {
  name   = "sns-topic"
  policy = data.aws_iam_policy_document.sns_policy.json
  user   = aws_iam_user.sns_user.name
}

resource "aws_kms_key" "sns_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sns queue"
}

resource "aws_kms_alias" "sns_key_alias" {
  name          = "alias/${var.environment}-sns-key"
  target_key_id = aws_kms_key.sns_key.arn
}
