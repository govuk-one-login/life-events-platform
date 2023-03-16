security_rules = [
  {
    rule            = "aws-foundational-security-best-practices/v/1.0.0/IAM.6"
    disabled_reason = "For our development account we do not need to enforce this"
  },
  {
    rule            = "cis-aws-foundations-benchmark/v/1.4.0/1.6"
    disabled_reason = "For our development account we do not need to enforce this"
  }
]
