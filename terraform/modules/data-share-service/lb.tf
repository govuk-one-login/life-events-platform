resource "aws_lb" "load_balancer" {
  name               = "${var.environment}-lb"
  load_balancer_type = "application"
  subnets            = module.vpc.public_subnet_ids
  security_groups    = aws_security_group.lb_auto.*.id
}

# We would use name_prefix, but it has a length limit of 6 characters
resource "random_id" "payments_http_sufix" {
  byte_length = 2
}

resource "aws_lb_listener" "listener-http" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.green.arn
  }

  depends_on = [aws_lb_target_group.green]
}

resource "aws_lb_target_group" "green" {
  name        = "${var.environment}-green-${random_id.payments_http_sufix.hex}"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health/ping"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_target_group" "blue" {
  name        = "${var.environment}-blue-${random_id.payments_http_sufix.hex}"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/health/ping"
  }

  lifecycle {
    create_before_destroy = true
  }
}