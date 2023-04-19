locals {
  # Adding HTTPS between cloudfront and load balancers breaks the cloudfront assigned domain name
  # DWP are using this URL on dev, so we only want to turn on HTTPS for demo for now
  is_dev = var.environment == "dev"
}

resource "aws_lb" "load_balancer" {
  name                       = "${var.environment}-lb"
  load_balancer_type         = "application"
  subnets                    = module.vpc.private_subnet_ids
  security_groups            = [aws_security_group.lb.id]
  drop_invalid_header_fields = true
  enable_deletion_protection = true
  internal                   = true

  access_logs {
    bucket  = module.lb_access_logs.id
    enabled = true
  }
}

module "lb_access_logs" {
  source = "../s3"

  account_id      = data.aws_caller_identity.current.account_id
  region          = var.region
  prefix          = var.environment
  name            = "lb-access-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  use_kms = false

  sns_arn = module.sns.topic_arn

  depends_on = [module.sns]
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
  port              = 80
  protocol          = "HTTP"

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
  port              = 8080
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.green.arn
  }

  lifecycle {
    ignore_changes = [default_action]
  }

  depends_on = [aws_lb_target_group.blue]
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

resource "aws_lb" "network_load_balancer" {
  name                       = "${var.environment}-nlb"
  load_balancer_type         = "network"
  subnets                    = module.vpc.public_subnet_ids
  security_groups            = [aws_security_group.nlb.id]
  drop_invalid_header_fields = true
  enable_deletion_protection = true

  access_logs {
    bucket  = module.nlb_access_logs.id
    enabled = true
  }
}

module "nlb_access_logs" {
  source = "../s3"

  account_id      = data.aws_caller_identity.current.account_id
  region          = var.region
  prefix          = var.environment
  name            = "nlb-access-logs"
  suffix          = "gdx-data-share-poc"
  expiration_days = 180

  use_kms = false

  sns_arn = module.sns.topic_arn

  depends_on = [module.sns]
}

resource "aws_lb_listener" "network_listener_https" {
  load_balancer_arn = aws_lb.network_load_balancer.arn
  port              = local.is_dev ? 80 : 443
  protocol          = local.is_dev ? "HTTP" : "HTTPS"
  ssl_policy        = local.is_dev ? null : "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = local.is_dev ? null : aws_acm_certificate_validation.acm_lb_certificate_validation.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.network.arn
  }

  lifecycle {
    ignore_changes = [default_action]
  }
}

resource "aws_lb_target_group" "network" {
  name        = "${var.environment}-network"
  target_type = "alb"
  port        = 80
  protocol    = "TCP"
  vpc_id      = module.vpc.vpc_id
}
