output "service_url" {
  value = aws_apprunner_service.lev_api.service_url
}

output "lev_rds_db_username" {
  value = aws_rds_cluster.lev_rds_postgres_cluster.master_username
}

output "lev_rds_db_password" {
  value     = aws_rds_cluster.lev_rds_postgres_cluster.master_password
  sensitive = true
}

output "lev_rds_db_name" {
  value = aws_rds_cluster.lev_rds_postgres_cluster.database_name
}

output "lev_rds_db_host" {
  value = aws_rds_cluster.lev_rds_postgres_cluster.endpoint
}
