data "aws_iam_policy_document" "support" {
  statement {
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = [var.account_id]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "support" {
  name               = "AWSSupportRole"
  assume_role_policy = data.aws_iam_policy_document.support.json
}

data "aws_iam_policy" "support_access" {
  name = "AWSSupportAccess"
}

resource "aws_iam_role_policy_attachment" "support_access" {
  role       = aws_iam_role.support.name
  policy_arn = data.aws_iam_policy.support_access.arn
}
