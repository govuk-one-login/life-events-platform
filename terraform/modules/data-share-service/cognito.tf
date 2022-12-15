module "cognito" {
  source      = "../cognito"
  environment = var.environment
  region      = var.region
}