resource "random_string" "rds_username" {
  length  = 8
  special = false
}

resource "random_password" "rds_password" {
  length  = 16
  special = false
}

resource "aws_rds_cluster" "rds_postgres_cluster" {
  cluster_identifier = "${var.environment}-rds-db"
  engine             = "aurora-postgresql"
  availability_zones = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]
  database_name      = "${var.environment}rdsdb"
  master_username    = random_string.rds_username.result
  master_password    = random_password.rds_password.result

  backup_retention_period = 5
  preferred_backup_window = "07:00-09:00"

  kms_key_id          = aws_kms_key.rds_key.arn
  storage_encrypted   = true
  skip_final_snapshot = true

  vpc_security_group_ids = [aws_security_group.rds_postgres_cluster.id]
}

resource "aws_security_group" "rds_postgres_cluster" {
  name_prefix = "${var.environment}-rds-postgres-cluster-"
  description = "For RDS cluster, inbound access from ECS only"
  vpc_id      = module.vpc.vpc_id

  ingress {
    protocol        = "tcp"
    from_port       = 5432
    to_port         = 5432
    security_groups = [aws_security_group.ecs_tasks.id]
    description     = "ECS task ingress rule, allow access from ECS tasks only"
  }

  egress {
    protocol        = "tcp"
    from_port       = 5432
    to_port         = 5432
    security_groups = [aws_security_group.ecs_tasks.id]
    description     = "ECS task egress rule, allow access to ECS tasks only"
  }

  lifecycle {
    create_before_destroy = true
  }
}