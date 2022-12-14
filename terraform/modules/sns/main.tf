data "aws_caller_identity" "current" {}

resource "aws_sns_topic" "topic" {
  name_prefix       = "${var.environment}-gdx-sns-topic"
  display_name      = var.topic_display_name
  kms_master_key_id = aws_kms_key.kms_key.arn
}

resource "aws_iam_user" "sns_user" {
  name = "${var.environment}-sns-topic-${var.topic_display_name}"
}

resource "aws_iam_access_key" "user" {
  user = aws_iam_user.user.sns_user
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

resource "aws_iam_user_policy" "policy" {
  name   = "sns-topic"
  policy = data.aws_iam_policy_document.policy.json
  user   = aws_iam_user.user.name
}

resource "aws_kms_key" "kms_key" {
  enable_key_rotation = true
  description         = "Key used to encrypt sns queue"
}
