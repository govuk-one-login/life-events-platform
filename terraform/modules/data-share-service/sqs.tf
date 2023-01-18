module "data_processor_queue" {
  source      = "../sqs"
  queue_name  = "${var.environment}-gdx-data-share-poc-data-processor-queue"
  environment = var.environment
}

module "audit_queue" {
  source      = "../sqs"
  queue_name  = "${var.environment}-gdx-data-share-poc-audit-queue"
  environment = var.environment
}
