data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::622626885786:user/${var.username}@digital.cabinet-office.gov.uk"]
    }

    actions = ["sts:AssumeRole"]

    condition {
      test     = "Bool"
      variable = "aws:MultiFactorAuthPresent"
      values   = ["true"]
    }
  }
}

resource "aws_iam_role" "role" {
  name               = "${var.username}-${var.role_suffix}"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

resource "aws_iam_role_policy_attachment" "policy_attachment" {
  for_each = var.policy_arns

  role       = aws_iam_role.role.name
  policy_arn = each.value
}
