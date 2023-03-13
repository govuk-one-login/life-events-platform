resource "random_string" "rds_username" {
  length  = 8
  special = false
}

resource "random_password" "rds_password" {
  length  = 16
  special = false
}

resource "aws_rds_cluster" "rds_postgres_cluster" {
  cluster_identifier                  = "${var.environment}-rds-db"
  engine                              = "aurora-postgresql"
  availability_zones                  = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]
  database_name                       = "${var.environment}rdsdb"
  master_username                     = random_string.rds_username.result
  master_password                     = random_password.rds_password.result
  iam_database_authentication_enabled = true

  backup_retention_period = 5
  preferred_backup_window = "07:00-09:00"

  kms_key_id          = aws_kms_key.rds_key.arn
  storage_encrypted   = true
  skip_final_snapshot = true

  vpc_security_group_ids          = [aws_security_group.rds_postgres_cluster.id]
  db_subnet_group_name            = aws_db_subnet_group.rds_postgres_cluster.name
  enabled_cloudwatch_logs_exports = ["postgresql"]

  serverlessv2_scaling_configuration {
    min_capacity = 0.5
    max_capacity = 2.0
  }
}

data "aws_iam_policy_document" "rds_enhanced_monitoring_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["monitoring.rds.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "rds_enhanced_monitoring" {
  name               = "${var.environment}-rds-enhanced-monitoring"
  assume_role_policy = data.aws_iam_policy_document.rds_enhanced_monitoring_assume_role.json
}

data "aws_iam_policy" "rds_enhanced_monitoring" {
  name = "AmazonRDSEnhancedMonitoringRole"
}

resource "aws_iam_role_policy_attachment" "rds_enhanced_monitoring" {
  role       = aws_iam_role.rds_enhanced_monitoring.name
  policy_arn = data.aws_iam_policy.rds_enhanced_monitoring.arn
}

resource "aws_rds_cluster_instance" "db_aurora-az1" {
  identifier         = "${var.environment}-rds-db-az1"
  cluster_identifier = aws_rds_cluster.rds_postgres_cluster.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.rds_postgres_cluster.engine
  engine_version     = aws_rds_cluster.rds_postgres_cluster.engine_version

  monitoring_interval = 30
  monitoring_role_arn = aws_iam_role.rds_enhanced_monitoring.arn

  performance_insights_enabled    = true
  performance_insights_kms_key_id = aws_kms_key.rds_key.arn

  auto_minor_version_upgrade = true
}

resource "aws_rds_cluster_instance" "db_aurora_az2" {
  identifier         = "${var.environment}-rds-db-az2"
  cluster_identifier = aws_rds_cluster.rds_postgres_cluster.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.rds_postgres_cluster.engine
  engine_version     = aws_rds_cluster.rds_postgres_cluster.engine_version

  monitoring_interval = 30
  monitoring_role_arn = aws_iam_role.rds_enhanced_monitoring.arn

  performance_insights_enabled    = true
  performance_insights_kms_key_id = aws_kms_key.rds_key.arn

  auto_minor_version_upgrade = true
}

resource "aws_security_group" "rds_postgres_cluster" {
  name_prefix = "${var.environment}-rds-postgres-cluster-"
  description = "For RDS cluster, inbound access from ECS or bastion host only"
  vpc_id      = module.vpc.vpc_id

  ingress {
    protocol        = "tcp"
    from_port       = 5432
    to_port         = 5432
    security_groups = [aws_security_group.ecs_tasks.id, aws_security_group.rds_bastion_host_sg.id]
    description     = "ECS task ingress rule, allow access from ECS tasks or bastion host only"
  }

  egress {
    protocol        = "tcp"
    from_port       = 5432
    to_port         = 5432
    security_groups = [aws_security_group.ecs_tasks.id, aws_security_group.rds_bastion_host_sg.id]
    description     = "ECS task egress rule, allow access to ECS tasks or bastion host only"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_db_subnet_group" "rds_postgres_cluster" {
  name       = "${var.environment}-rds-postgres-cluster-subnet"
  subnet_ids = module.vpc.private_subnet_ids
}
