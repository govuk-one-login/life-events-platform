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
  environment     = "grafana"
  name            = "lb-access-logs"
  expiration_days = 180

  use_kms = false

  sns_arn = var.sns_topic_arn
}

#tfsec:ignore:aws-elb-http-not-used
resource "aws_lb_listener" "listener_http" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 80
  protocol          = "HTTP"

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
  from_port         = 80
  to_port           = 80
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
