data "aws_iam_policy_document" "control_tower_assume_role_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::892537467220:root"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "control_tower_role" {
  name               = "AWSControlTowerExecution"
  assume_role_policy = data.aws_iam_policy_document.control_tower_assume_role_policy.json
}

data "aws_iam_policy" "administrator_access" {
  name = "AdministratorAccess"
}

resource "aws_iam_role_policy_attachment" "control_tower_policy_attachment" {
  role       = aws_iam_role.control_tower_role.name
  policy_arn = data.aws_iam_policy.administrator_access.arn
}
