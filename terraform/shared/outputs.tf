output "grafana_task_role_name" {
  value = module.grafana.task_role_name
}
output "hosted_zone_name" {
  value = module.route53.name
}
output "hosted_zone_name_servers" {
  value = module.route53.name_servers
}
