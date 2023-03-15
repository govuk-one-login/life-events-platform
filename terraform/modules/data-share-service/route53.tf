resource "aws_route53_zone" "zone" {
  name = "${var.environment}.${var.top_level_route53_zone_name}"
}

resource "aws_acm_certificate" "hosted_zone" {
  domain_name       = aws_route53_zone.zone.name
  validation_method = "DNS"
}

resource "aws_route53_record" "cloudfront" {
  zone_id = aws_route53_zone.zone.zone_id
  name    = aws_route53_zone.zone.name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.gdx_data_share_poc.domain_name
    zone_id                = aws_cloudfront_distribution.gdx_data_share_poc.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "subdomain" {
  zone_id = var.top_level_route53_zone_id
  name    = aws_route53_zone.zone.name
  type    = "NS"
  ttl     = 30

  records = aws_route53_zone.zone.name_servers
}
