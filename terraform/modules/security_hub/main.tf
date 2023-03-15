resource "aws_securityhub_account" "securityhub" {}

resource "aws_securityhub_standards_subscription" "cis" {
  depends_on    = [aws_securityhub_account.securityhub]
  standards_arn = "arn:aws:securityhub:${var.region}::standards/cis-aws-foundations-benchmark/v/1.4.0"
}

resource "aws_securityhub_finding_aggregator" "securityhub" {
  depends_on   = [aws_securityhub_account.securityhub]
  linking_mode = "ALL_REGIONS"
}
