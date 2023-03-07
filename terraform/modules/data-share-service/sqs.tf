module "supplier_event_queue" {
  source      = "../sqs"
  queue_name  = "${var.environment}-gdx-data-share-supplier-events"
  environment = var.environment
}

module "acquirer_event_queue" {
  source      = "../sqs"
  queue_name  = "${var.environment}-gdx-data-share-acquirer-events"
  environment = var.environment
}
