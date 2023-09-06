locals {
  waf_acl_name       = "${var.environment}-gdx-data-share-poc"
  waf_acl_name_short = join("", split("-", local.waf_acl_name))

  # Check the contents using both HTML-encoded and URL-encoded versions
  # Applies to the SQL Injection rules
  waf_rules_transforms = [
    "HTML_ENTITY_DECODE",
    "URL_DECODE",
  ]
  waf_rules_fields = [ # values are [ field name, max size limit ]
    ["body", 65536],
    ["cookies", 8192],
    ["query_string", 1024],
    ["uri_path", 1024],
  ]
  waf_transform_field_pairs = setproduct(local.waf_rules_transforms, local.waf_rules_fields)

  waf_rules_max_sizes = {
    "BODY"          = 65536,
    "HEADER.cookie" = 8192,
    "QUERY_STRING"  = 1024,
    "URI"           = 1024,
  }

  blocked_ips = []
}

resource "aws_wafv2_ip_set" "blocked_ipset" {
  provider           = aws.us-east-1 # To work with CloudFront, you must also specify the region us-east-1 (N. Virginia) on the AWS provider
  name               = "${local.waf_acl_name}-blocked-ipset"
  scope              = "CLOUDFRONT"
  ip_address_version = "IPV4"
  addresses          = local.blocked_ips
}

resource "aws_wafv2_web_acl" "cloudfront" {
  name     = local.waf_acl_name
  scope    = "CLOUDFRONT"
  provider = aws.us-east-1 # To work with CloudFront, you must also specify the region us-east-1 (N. Virginia) on the AWS provider

  default_action {
    allow {}
  }

  dynamic "rule" {
    for_each = local.waf_rules_fields
    iterator = field
    content {
      name     = "${local.waf_acl_name}-max-field-size-${field.value[0]}"
      priority = field.key + 1

      action {
        block {}
      }

      statement {
        size_constraint_statement {
          dynamic "field_to_match" {
            for_each = field.value[0] == "body" ? [""] : []
            content {
              body {}
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "cookies" ? [""] : []
            content {
              cookies {
                match_pattern {
                  all {}
                }
                match_scope       = "ALL"
                oversize_handling = "MATCH"
              }
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "query_string" ? [""] : []
            content {
              query_string {}
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "uri_path" ? [""] : []
            content {
              uri_path {}
            }
          }

          text_transformation {
            priority = 1
            type     = "NONE"
          }
          comparison_operator = "GT"
          size                = field.value[1]
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "${local.waf_acl_name_short}maxfieldsize${join("", split("_", field.value[0]))}"
        sampled_requests_enabled   = true
      }
    }
  }

  dynamic "rule" {
    for_each = local.waf_rules_fields
    iterator = field
    content {
      name     = "${local.waf_acl_name}-sql-injection-${field.value[0]}"
      priority = length(local.waf_rules_fields) + field.key + 1

      action {
        block {}
      }

      statement {
        sqli_match_statement {
          dynamic "field_to_match" {
            for_each = field.value[0] == "body" ? [""] : []
            content {
              body {}
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "cookies" ? [""] : []
            content {
              cookies {
                match_pattern {
                  all {}
                }
                match_scope       = "ALL"
                oversize_handling = "MATCH"
              }
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "query_string" ? [""] : []
            content {
              query_string {}
            }
          }
          dynamic "field_to_match" {
            for_each = field.value[0] == "uri_path" ? [""] : []
            content {
              uri_path {}
            }
          }

          dynamic "text_transformation" {
            # Create a match for each transform
            for_each = local.waf_rules_transforms
            iterator = transform
            content {
              type     = transform.value
              priority = transform.key + 1
            }
          }
        }
      }

      visibility_config {
        cloudwatch_metrics_enabled = true
        metric_name                = "${local.waf_acl_name_short}sqlinjection${join("", split("_", field.value[0]))}"
        sampled_requests_enabled   = true
      }
    }
  }

  rule {
    name     = "${local.waf_acl_name}-blocked-ips-rule"
    priority = length(local.waf_rules_fields) + length(local.waf_rules_fields) + 1

    action {
      block {}
    }

    statement {
      ip_set_reference_statement {
        arn = aws_wafv2_ip_set.blocked_ipset.arn
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${local.waf_acl_name_short}blockedipsrule"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = local.waf_acl_name_short
    sampled_requests_enabled   = true
  }

  depends_on = [
    aws_wafv2_ip_set.blocked_ipset
  ]
}

resource "aws_wafv2_web_acl" "load_balancer" {
  name  = "${var.environment}-load-balancer"
  scope = "REGIONAL"

  default_action {
    allow {}
  }

  rule {
    action {
      block {}
    }

    name     = "SqlInjectionMatchQuery"
    priority = 1

    statement {
      sqli_match_statement {
        text_transformation {
          type     = "URL_DECODE"
          priority = 1
        }

        field_to_match {
          query_string {}
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "SQLInjectionQuery"
      sampled_requests_enabled   = true
    }
  }

  rule {
    action {
      block {}
    }

    name     = "SqlInjectionMatchBody"
    priority = 2

    statement {
      sqli_match_statement {
        text_transformation {
          type     = "NONE"
          priority = 1
        }

        field_to_match {
          body {}
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "SQLInjectionBody"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.environment}-load-balancer"
    sampled_requests_enabled   = true
  }
}

resource "aws_wafv2_web_acl_association" "waf_load_balancer" {
  resource_arn = aws_lb.load_balancer.arn
  web_acl_arn  = aws_wafv2_web_acl.load_balancer.arn
}

# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-logging
#tfsec:ignore:aws-s3-enable-versioning
module "waf_lb_logs_bucket" {
  source = "../s3"

  account_id      = data.aws_caller_identity.current.account_id
  region          = var.region
  prefix          = "aws-waf-logs-${var.environment}"
  name            = "load-balancer"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket      = false
  allow_delivery_logs = true

  sns_arn = module.sns.topic_arn
}

resource "aws_wafv2_web_acl_logging_configuration" "load_balancer" {
  log_destination_configs = [module.waf_lb_logs_bucket.arn]
  resource_arn            = aws_wafv2_web_acl.load_balancer.arn

  depends_on = [module.waf_lb_logs_bucket]
}

# This is the logging bucket, it doesn't need logs or versioning
#tfsec:ignore:aws-s3-enable-logging
#tfsec:ignore:aws-s3-enable-versioning
module "waf_cloudfront_logs_bucket" {
  source = "../s3"

  account_id      = data.aws_caller_identity.current.account_id
  region          = var.region
  prefix          = "aws-waf-logs-${var.environment}"
  name            = "cloudfront"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket      = false
  allow_delivery_logs = true

  sns_arn = module.sns.topic_arn
}

resource "aws_wafv2_web_acl_logging_configuration" "cloudfront" {
  provider = aws.us-east-1

  log_destination_configs = [module.waf_cloudfront_logs_bucket.arn]
  resource_arn            = aws_wafv2_web_acl.cloudfront.arn

  depends_on = [module.waf_cloudfront_logs_bucket]
}
