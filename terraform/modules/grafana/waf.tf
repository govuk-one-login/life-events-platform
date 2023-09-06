resource "aws_wafv2_web_acl" "cloudfront" {
  name     = "grafana-cloudfront"
  scope    = "CLOUDFRONT"
  provider = aws.us-east-1
  # To work with CloudFront, you must also specify the region us-east-1 (N. Virginia) on the AWS provider

  default_action {
    allow {}
  }

  rule {
    name     = "RateLimit"
    priority = 1

    action {
      block {}
    }

    statement {

      rate_based_statement {
        aggregate_key_type = "IP"
        limit              = 500
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "RateLimit"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "grafana-cloudfront"
    sampled_requests_enabled   = true
  }
}

resource "aws_wafv2_web_acl" "load_balancer" {
  name  = "grafana-load-balancer"
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
    metric_name                = "grafana-load-balancer"
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

  account_id      = var.account_id
  region          = var.region
  prefix          = "aws-waf-logs-grafana"
  name            = "load-balancer"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket      = false
  allow_delivery_logs = true

  sns_arn = var.s3_event_notification_sns_topic_arn
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

  account_id      = var.account_id
  region          = var.region
  prefix          = "aws-waf-logs-grafana"
  name            = "cloudfront"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket = false

  sns_arn = var.s3_event_notification_sns_topic_arn
}

resource "aws_wafv2_web_acl_logging_configuration" "cloudfront" {
  provider = aws.us-east-1

  log_destination_configs = [module.waf_cloudfront_logs_bucket.arn]
  resource_arn            = aws_wafv2_web_acl.cloudfront.arn

  depends_on = [module.waf_cloudfront_logs_bucket]
}

