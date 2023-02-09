resource "aws_security_group" "ecs_tasks" {
  name        = "${var.environment}-ecs-tasks"
  description = "For GDX Data Share PoC Service ECS tasks, inbound access from GDX LB only"
  vpc_id      = module.vpc.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "ecs_tasks_ingress" {
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 8080
  to_port                  = 8080
  source_security_group_id = aws_security_group.lb.id
  description              = "ECS task ingress rule, allow access from LB"
  security_group_id        = aws_security_group.ecs_tasks.id
}

#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group_rule" "ecs_tasks_https_egress" {
  type              = "egress"
  protocol          = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "ECS task egress rule for HTTPS"
  security_group_id = aws_security_group.ecs_tasks.id
}

resource "aws_security_group_rule" "ecs_tasks_rds_egress" {
  type                     = "egress"
  protocol                 = "tcp"
  from_port                = 5432
  to_port                  = 5432
  source_security_group_id = aws_security_group.rds_postgres_cluster.id
  description              = "ECS task egress rule to RDS"
  security_group_id        = aws_security_group.ecs_tasks.id
}
