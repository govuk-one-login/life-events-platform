# We want the load balancer available to our cloudfront distribution, it is locked down from the wider internet with
# security group rules
#tfsec:ignore:aws-elb-alb-not-public
resource "aws_lb" "load_balancer" {
  name                       = "grafana-lb"
  load_balancer_type         = "application"
  subnets                    = var.public_subnet_ids
  security_groups            = [aws_security_group.lb.id]
  drop_invalid_header_fields = true
  enable_deletion_protection = true

  access_logs {
    bucket  = module.lb_access_logs.id
    enabled = true
  }
}

module "lb_access_logs" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = "grafana"
  name            = "lb-access-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  use_kms       = false
  allow_lb_logs = true

  sns_arn = var.s3_event_notification_sns_topic_arn
}

resource "aws_acm_certificate" "acm_lb_certificate" {
  domain_name       = var.hosted_zone_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "lb_cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.acm_lb_certificate.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = var.hosted_zone_id
}

resource "aws_acm_certificate_validation" "acm_lb_certificate_validation" {
  certificate_arn         = aws_acm_certificate.acm_lb_certificate.arn
  validation_record_fqdns = [for record in aws_route53_record.lb_cert_validation : record.fqdn]
}

resource "aws_lb_listener" "listener_http" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = aws_acm_certificate_validation.acm_lb_certificate_validation.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.grafana.arn
  }

  lifecycle {
    ignore_changes = [default_action]
  }

  depends_on = [aws_lb_target_group.grafana]
}

resource "aws_lb_target_group" "grafana" {
  name        = "grafana-target"
  port        = 3000
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.vpc_id

  health_check {
    path = "/api/health"
  }

  lifecycle {
    create_before_destroy = true
  }
}
resource "aws_security_group" "lb" {
  name        = "grafana-lb"
  description = "Allow access to Grafana LB from Cloudfront"
  vpc_id      = var.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

resource "aws_security_group_rule" "lb_cloudfront" {
  type              = "ingress"
  protocol          = "tcp"
  from_port         = 443
  to_port           = 443
  description       = "LB ingress rule for cloudfront"
  security_group_id = aws_security_group.lb.id
  prefix_list_ids   = [data.aws_ec2_managed_prefix_list.cloudfront.id]
}

resource "aws_security_group_rule" "lb_egress" {
  type              = "egress"
  from_port         = 3000
  to_port           = 3000
  protocol          = "tcp"
  description       = "LB egress rule to VPC"
  security_group_id = aws_security_group.lb.id
  cidr_blocks       = [var.vpc_cidr]
}
