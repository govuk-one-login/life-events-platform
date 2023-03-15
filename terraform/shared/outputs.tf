output "grafana_task_role_name" {
  value = module.grafana.task_role_name
}

output "route53_zone_id" {
  value = aws_route53_zone.zone.zone_id
}

output "route53_zone_name" {
  value = aws_route53_zone.zone.name
}
