data "aws_iam_policy_document" "cross_account_access_policy" {
  statement {
    sid    = "Allow cross account access"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = var.cross_account_arns
    }

    actions = [
      "s3:GetObject",
      "s3:ListBucket",
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "cloudtrail_access_policy" {
  statement {
    sid    = "Allow cloudtrail logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudtrail.amazonaws.com"]
    }

    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "delivery_log_access_policy" {
  statement {
    sid    = "Allow delivery logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["delivery.logs.amazonaws.com"]
    }
    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "lb_access_policy" {
  statement {
    sid    = "Allow LB logs"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::652711504416:root"]
    }
    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

data "aws_iam_policy_document" "config_access_policy" {
  statement {
    sid    = "Allow Config logs"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["config.amazonaws.com"]
    }
    actions = [
      "s3:PutObject",
      "s3:GetBucketAcl",
      "s3:ListBucket"
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
  }
}

locals {
  cross_account_policy = length(var.cross_account_arns) != 0 ? [data.aws_iam_policy_document.cross_account_access_policy.json] : []
  cloudtrail_policy    = var.allow_cloudtrail_logs ? [data.aws_iam_policy_document.cloudtrail_access_policy.json] : []
  delivery_log_policy  = var.allow_delivery_logs ? [data.aws_iam_policy_document.delivery_log_access_policy.json] : []
  lb_policy            = var.allow_lb_logs ? [data.aws_iam_policy_document.lb_access_policy.json] : []
  config_policy        = var.allow_config_logs ? [data.aws_iam_policy_document.config_access_policy.json] : []
  source_policies = concat(
    local.cross_account_policy,
    local.cloudtrail_policy,
    local.delivery_log_policy,
    local.lb_policy,
    local.config_policy
  )
}

data "aws_iam_policy_document" "bucket_policy" {
  source_policy_documents = local.source_policies

  statement {
    sid    = "Deny Insecure Transport"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values = [
        "false"
      ]
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = aws_s3_bucket.bucket.id
  policy = data.aws_iam_policy_document.bucket_policy.json
}
moved {
  from = aws_s3_bucket_policy.deny_insecure_transport
  to   = aws_s3_bucket_policy.bucket_policy
}
