#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group" "ecs_tasks" {
  name_prefix = "${var.environment}-ecs-tasks-"
  description = "For GDX Data Share PoC Service ECS tasks, inbound access from GDX LB only"
  vpc_id      = module.vpc.vpc_id

  ingress {
    protocol        = "tcp"
    from_port       = 8080
    to_port         = 8080
    security_groups = aws_security_group.lb_auto.*.id
    description     = "ECS task ingress rule, allow access from LB only"
  }

  egress {
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = ["0.0.0.0/0"]
    description = "ECS task egress rule"
  }

  lifecycle {
    create_before_destroy = true
  }
}
