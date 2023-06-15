resource "aws_wafv2_web_acl" "ip_restrict_user_login" {
  count       = var.ip_allowlist == null ? 0 : 1
  name        = "${var.environment}-cognito-user-pool-data-share"
  description = "ACL to restrict access to user auth to ${aws_cognito_user_pool.pool.arn} to a specific set of IP addresses"
  scope       = "REGIONAL"

  default_action {
    block {}
  }

  rule {
    name     = "allow-token-requests"
    priority = 1
    action {
      allow {}
    }
    statement {
      byte_match_statement {
        field_to_match {
          uri_path {}
        }
        positional_constraint = "EXACTLY"
        search_string         = "/oauth2/token"
        text_transformation {
          priority = 0
          type     = "NONE"
        }
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.environment}-cognito-waf-allowed-tokens"
      sampled_requests_enabled   = false
    }
  }
  rule {
    name     = "allow-ip-list"
    priority = 2
    action {
      allow {}
    }
    statement {
      ip_set_reference_statement {
        arn = aws_wafv2_ip_set.allowed_ips[0].arn
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.environment}-cognito-waf-allowed-ips"
      sampled_requests_enabled   = false
    }
  }
  rule {
    name     = "allow-2fa"
    priority = 3
    action {
      allow {}
    }
    statement {
      and_statement {
        statement {
          byte_match_statement {
            field_to_match {
              uri_path {}
            }
            positional_constraint = "EXACTLY"
            search_string         = "/"
            text_transformation {
              priority = 0
              type     = "NONE"
            }
          }
        }
        statement {
          byte_match_statement {
            field_to_match {
              method {}
            }
            positional_constraint = "EXACTLY"
            search_string         = "POST"
            text_transformation {
              priority = 0
              type     = "NONE"
            }
          }
        }
        statement {
          or_statement {
            statement {
              byte_match_statement {
                field_to_match {
                  single_header {
                    name = "x-amzn-cognito-operation-name"
                  }
                }
                positional_constraint = "EXACTLY"
                search_string         = "AssociateSoftwareToken"
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
            statement {
              byte_match_statement {
                field_to_match {
                  single_header {
                    name = "x-amzn-cognito-operation-name"
                  }
                }
                positional_constraint = "EXACTLY"
                search_string         = "VerifySoftwareToken"
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
      }
    }
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.environment}-cognito-waf-allow-2fa"
      sampled_requests_enabled   = false
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.environment}-cognito-waf"
    sampled_requests_enabled   = false
  }
}

resource "aws_wafv2_ip_set" "allowed_ips" {
  count = var.ip_allowlist == null ? 0 : 1

  ip_address_version = "IPV4"
  name               = "${var.environment}-cognito-user-pool-data-share"
  description        = "IP addresses allowed to authenticate as user via cognito user pool in ${var.environment}"
  scope              = "REGIONAL"
  addresses          = var.ip_allowlist
}

resource "aws_wafv2_web_acl_association" "user_pool" {
  count = var.ip_allowlist == null ? 0 : 1

  resource_arn = aws_cognito_user_pool.pool.arn
  web_acl_arn  = aws_wafv2_web_acl.ip_restrict_user_login[0].arn
}
