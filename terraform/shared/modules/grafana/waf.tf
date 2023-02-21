resource "aws_wafv2_web_acl" "gdx_data_share_poc" {
  name     = "grafana"
  scope    = "CLOUDFRONT"
  provider = aws.us-east-1
  # To work with CloudFront, you must also specify the region us-east-1 (N. Virginia) on the AWS provider

  default_action {
    allow {}
  }


  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "grafana"
    sampled_requests_enabled   = true
  }
}
