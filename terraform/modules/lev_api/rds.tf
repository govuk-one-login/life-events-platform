resource "random_string" "rds_username" {
  length  = 8
  special = false
}

resource "random_password" "rds_password" {
  length  = 16
  special = false
}

#tfsec:ignore:aws-rds-no-public-db-access
resource "aws_rds_cluster" "lev_rds_postgres_cluster" {
  cluster_identifier = "${var.environment}-lev-rds-db"
  engine             = "aurora-postgresql"
  database_name      = "${var.environment}rdsdb"
  master_username    = random_string.rds_username.result
  master_password    = random_password.rds_password.result

  backup_retention_period = 5
  preferred_backup_window = "07:00-09:00"

  kms_key_id          = aws_kms_key.rds_lev_key.arn
  storage_encrypted   = true
  skip_final_snapshot = true

  vpc_security_group_ids          = [aws_security_group.lev_api.id]
  enabled_cloudwatch_logs_exports = ["postgresql"]

  serverlessv2_scaling_configuration {
    min_capacity = 0.5
    max_capacity = 2.0
  }
}

resource "aws_rds_cluster_instance" "db_aurora" {
  identifier         = "${var.environment}-lev-rds-db"
  cluster_identifier = aws_rds_cluster.lev_rds_postgres_cluster.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.lev_rds_postgres_cluster.engine
  engine_version     = aws_rds_cluster.lev_rds_postgres_cluster.engine_version

  publicly_accessible             = true
  performance_insights_enabled    = true
  performance_insights_kms_key_id = aws_kms_key.rds_lev_key.arn
}

resource "aws_kms_key" "rds_lev_key" {
  description         = "Key for RDS encryption"
  enable_key_rotation = true
}

resource "aws_kms_alias" "rds_lev_key_alias" {
  name          = "alias/${var.environment}/lev-rds-cluster"
  target_key_id = aws_kms_key.rds_lev_key.key_id
}
