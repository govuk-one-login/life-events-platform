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
#tfsec:ignore:aws-s3-enable-bucket-logging
#tfsec:ignore:aws-s3-enable-versioning
module "waf_lb_logs_bucket" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = "aws-waf-logs-grafana"
  name            = "load-balancer"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  add_log_bucket = false

  sns_arn = var.s3_event_notification_sns_topic_arn
}

resource "aws_wafv2_web_acl_logging_configuration" "load_balancer" {
  log_destination_configs = [module.waf_lb_logs_bucket.arn]
  resource_arn            = aws_wafv2_web_acl.load_balancer.arn

  depends_on = [module.waf_lb_logs_bucket]
}

