data "aws_iam_policy_document" "config_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["config.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "config" {
  name               = "aws-config"
  assume_role_policy = data.aws_iam_policy_document.config_assume_role.json
}

moved {
  from = aws_config_configuration_recorder.config
  to   = module.securityhub_local.aws_config_configuration_recorder.config
}

moved {
  from = aws_config_configuration_recorder_status.config
  to   = module.securityhub_local.aws_config_configuration_recorder_status.config
}

moved {
  from = aws_config_delivery_channel.config
  to   = module.securityhub_local.aws_config_delivery_channel.config
}

moved {
  from = aws_config_conformance_pack.cis_1_4
  to   = module.securityhub_local.aws_config_conformance_pack.cis_1_4
}

moved {
  from = aws_config_conformance_pack.ncsc_cloudsec_principles
  to   = module.securityhub_local.aws_config_conformance_pack.ncsc_cloudsec_principles
}

module "config_s3" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = "shared"
  name            = "config"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180
  sns_arn         = var.s3_event_notification_sns_topic_arn
}

data "aws_iam_policy_document" "config_s3" {
  statement {
    effect    = "Allow"
    actions   = ["s3:GetBucketAcl"]
    resources = [module.config_s3.arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:PutObjectAcl",
    ]
    resources = [module.config_s3.objects_arn]
  }
  statement {
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [module.config_s3.kms_arn]
  }
}

resource "aws_iam_policy" "config_s3" {
  name   = "config-s3"
  policy = data.aws_iam_policy_document.config_s3.json
}

resource "aws_iam_role_policy_attachment" "config_s3" {
  role       = aws_iam_role.config.name
  policy_arn = aws_iam_policy.config_s3.arn
}

data "aws_iam_policy" "config" {
  name = "AWS_ConfigRole"
}

resource "aws_iam_role_policy_attachment" "config" {
  role       = aws_iam_role.config.name
  policy_arn = data.aws_iam_policy.config.arn
}
