module "sns" {
  source             = "../sns"
  topic_display_name = "${var.environment}-gdx-data-share-poc-events"
  environment        = var.environment
}
