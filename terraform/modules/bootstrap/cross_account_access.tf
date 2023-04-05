data "aws_iam_policy_document" "cross_account_access" {
  statement {
    principals {
      type        = "AWS"
      identifiers = var.cross_account_arns
    }

    actions = [
      "s3:GetObject",
      "s3:ListBucket",
    ]

    resources = [
      aws_s3_bucket.state_bucket.arn,
      "${aws_s3_bucket.state_bucket.arn}/*",
    ]
  }
}
