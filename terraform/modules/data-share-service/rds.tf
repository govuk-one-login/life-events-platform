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
}
