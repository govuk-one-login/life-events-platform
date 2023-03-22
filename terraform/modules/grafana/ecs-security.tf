resource "aws_security_group" "ecs_task" {
  name        = "grafana-ecs-tasks"
  description = "For Grafana tasks, access from LB and EFS only"
  vpc_id      = var.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "ecs_tasks_ingress" {
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 3000
  to_port                  = 3000
  source_security_group_id = aws_security_group.lb.id
  description              = "ECS task ingress rule, allow access from LB"
  security_group_id        = aws_security_group.ecs_task.id
}

#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group_rule" "ecs_tasks_https_egress" {
  type              = "egress"
  protocol          = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "ECS task egress rule for HTTPS"
  security_group_id = aws_security_group.ecs_task.id
}

resource "aws_security_group_rule" "ecs_efs_egress" {
  type                     = "egress"
  protocol                 = "tcp"
  from_port                = 2049
  to_port                  = 2049
  source_security_group_id = aws_security_group.efs.id
  description              = "ECS task egress rule, allow access to EFS"
  security_group_id        = aws_security_group.ecs_task.id
}
