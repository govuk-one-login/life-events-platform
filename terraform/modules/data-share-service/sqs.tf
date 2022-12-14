module "data_processor_queue" {
  source      = "../sqs"
  queue_name        = "${var.environment}-gdx-data-share-poc-data-processor"
  environment = var.environment
}

module "audit_queue" {
  source      = "../sqs"
  queue_name        = "${var.environment}-gdx-data-share-poc-audit-queue"
  environment = var.environment
}

module "outbound_adaptor_queue" {
  source      = "../sqs"
  queue_name        = "${var.environment}-gdx-data-share-poc-outbound-queue"
  environment = var.environment
}

module "other_department_queue" {
  source      = "../sqs"
  queue_name        = "${var.environment}-gdx-data-share-poc-other-department-queue"
  environment = var.environment
}
