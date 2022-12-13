resource "aws_lb" "load_balancer" {
  name               = "${var.environment}-lb"
  load_balancer_type = "application"
  subnets            = module.vpc.public_subnet_ids
  security_groups    = aws_security_group.lb_auto.*.id
}

resource "aws_lb_listener" "listener-http" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.default.arn
  }
  #  default_action {
  #    type = "redirect"
  #    redirect {
  #      port        = "443"
  #      protocol    = "HTTPS"
  #      status_code = "HTTP_301"
  #    }
  #  }

  depends_on = [aws_lb_target_group.default]
}

#resource "aws_lb_listener" "listener-https" {
#  load_balancer_arn = aws_lb.load_balancer.arn
#  port              = "443"
#  protocol          = "HTTPS"
#  certificate_arn   = aws_cloudfront_distribution.gdx_data_share_poc.viewer_certificate[0].acm_certificate_arn
#
#  default_action {
#    type             = "forward"
#    target_group_arn = aws_lb_target_group.default.arn
#  }
#
#  lifecycle {
#    ignore_changes = [default_action]
#  }
#
#  depends_on = [
#    aws_lb.load_balancer,
#    aws_lb_target_group.default,
#    aws_cloudfront_distribution.gdx_data_share_poc
#  ]
#}

resource "aws_lb_target_group" "default" {
  name        = "${var.environment}-default-tg"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health/ping"
  }
}

resource "aws_lb_target_group" "green" {
  name        = "${var.environment}-green-tg"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health/ping"
  }
}