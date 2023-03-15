resource "aws_route53_zone" "zone" {
  name = "${var.environment}.share-life-events.service.gov.uk"
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
