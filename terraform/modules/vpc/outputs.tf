output "vpc_id" {
  value = module.vpc.vpc_id
}

output "private_subnet_ids" {
  value = module.vpc.private_subnet_ids
}

output "public_subnet_ids" {
  value = module.vpc.public_subnet_ids
}

output "private_cidr_blocks" {
  value = module.vpc.private_cidr_blocks
}

output "nat_gateway_public_eips" {
  value = module.vpc.nat_gateway_public_eips
}
