resource "aws_route53_zone" "zone" {
  name = var.hosted_zone_name
}

resource "aws_route53_record" "subdomains" {
  for_each = {
    for index, subdomain in var.subdomains :
    index => subdomain
  }

  zone_id = aws_route53_zone.zone.id
  name    = each.value.name
  type    = "NS"
  ttl     = 30

  records = each.value.name_servers
}
