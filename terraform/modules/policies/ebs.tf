resource "aws_ebs_encryption_by_default" "ebs_encryption" {
  enabled = true
}

resource "aws_ebs_encryption_by_default" "ebs_encryption_global" {
  enabled = true

  provider = aws.us-east-1
}

