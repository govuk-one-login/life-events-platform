#tfsec:ignore:aws-cloudfront-use-secure-tls-policy
resource "aws_cloudfront_distribution" "grafana" {
  provider = aws.us-east-1

  origin {
    domain_name = aws_lb.load_balancer.dns_name
    origin_id   = "grafana-lb"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      # Required but irrelevant - we do not use HTTPS to talk to LB
      origin_ssl_protocols = ["TLSv1.2"]
      # AWS enforce a maximum of 60s, but we can request more if desired.
      # See https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/distribution-web-values-specify.html#DownloadDistValuesOriginResponseTimeout
      origin_read_timeout = 60
    }
  }

  logging_config {
    include_cookies = false
    bucket          = "${module.cloudfront_logs_bucket.name}.s3.amazonaws.com"
    prefix          = "grafana"
  }

  enabled         = true
  is_ipv6_enabled = false

  default_cache_behavior {
    allowed_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"]

    forwarded_values {
      query_string = true
      headers      = ["*"]
      cookies {
        forward = "all"
      }
    }

    # Auto-compress stuff if given Accept-Encoding: gzip header
    compress = true

    min_ttl     = 0
    max_ttl     = 0
    default_ttl = 0

    # Required even though settings above mean we are not caching
    target_origin_id       = "grafana-lb"
    viewer_protocol_policy = "redirect-to-https"
    cached_methods         = ["GET", "HEAD", "OPTIONS"]

  }

  # This is the lowest class and only offers nodes in Europe, US and Asia.
  # Given that most of our traffic is from the UK, this should be sufficient.
  price_class = "PriceClass_100"

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  web_acl_id = aws_wafv2_web_acl.cloudfront.arn
}
