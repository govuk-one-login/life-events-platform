locals {
  # Adding HTTPS between cloudfront and load balancers breaks the cloudfront assigned domain name
  # DWP are using this URL on dev, so we only want to turn on HTTPS for demo for now
  is_dev = var.environment == "dev"
}

# We want the load balancer available to our cloudfront distribution, it is locked down from the wider internet with
# security group rules
#tfsec:ignore:aws-elb-alb-not-public
resource "aws_lb" "load_balancer" {
  name                       = "${var.environment}-lb"
  load_balancer_type         = "application"
  subnets                    = module.vpc.public_subnet_ids
  security_groups            = [aws_security_group.lb.id]
  drop_invalid_header_fields = true
  enable_deletion_protection = true
}

# We would use name_prefix, but it has a length limit of 6 characters
resource "random_id" "http_sufix" {
  byte_length = 2
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

#tfsec:ignore:aws-elb-http-not-used
resource "aws_lb_listener" "listener_https" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = local.is_dev ? 80 : 443
  protocol          = local.is_dev ? "HTTP" : "HTTPS"
  ssl_policy        = local.is_dev ? null : "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = local.is_dev ? null : aws_acm_certificate_validation.acm_lb_certificate_validation.certificate_arn


  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.green.arn
  }

  lifecycle {
    ignore_changes = [default_action]
  }

  depends_on = [aws_lb_target_group.green]
}

#tfsec:ignore:aws-elb-http-not-used
resource "aws_lb_listener" "test_listener_https" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = local.is_dev ? 8080 : 8443
  protocol          = local.is_dev ? "HTTP" : "HTTPS"
  ssl_policy        = local.is_dev ? null : "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = local.is_dev ? null : aws_acm_certificate_validation.acm_lb_certificate_validation.certificate_arn

  default_action {
    type = "fixed-response"

    fixed_response {
      status_code  = "403"
      content_type = "text/plain"
    }
  }

  lifecycle {
    ignore_changes = [default_action]
  }

  depends_on = [aws_lb_target_group.blue]
}

resource "random_password" "test_auth_header" {
  length  = 64
  special = false
}

resource "aws_lb_listener_rule" "protected_test_forward" {
  listener_arn = aws_lb_listener.test_listener_https.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.blue.arn
  }

  condition {
    http_header {
      values           = [random_password.test_auth_header.result]
      http_header_name = "X-TEST-AUTH"
    }
  }

  lifecycle {
    ignore_changes = [action]
  }
}

resource "aws_lb_target_group" "green" {
  name        = "${var.environment}-green-${random_id.http_sufix.hex}"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_target_group" "blue" {
  name        = "${var.environment}-blue-${random_id.http_sufix.hex}"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health"
  }

  lifecycle {
    create_before_destroy = true
  }
}
