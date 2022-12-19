resource "aws_ecs_cluster" "main" {
  name = var.environment

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

locals {
  rds_db_url = "postgresql://${aws_rds_cluster.rds_postgres_cluster.endpoint}:${aws_rds_cluster.rds_postgres_cluster.port}/${aws_rds_cluster.rds_postgres_cluster.database_name}"
}

resource "aws_ecs_task_definition" "gdx_data_share_poc" {
  family                   = "${var.environment}-gdx-data-share-poc"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  memory                   = 512
  cpu                      = 256
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "${var.environment}-gdx-data-share-poc",
      image        = "${var.ecr_url}/gdx-data-share-poc:${var.environment}",
      portMappings = [{ "containerPort" : 8080, "hostPort" : 8080 }],
      environment = [
        { "name" : "SERVER_PORT", "value" : "8080" },

        { "name" : "API_BASE_URL_LEV", "value" : "https://${var.lev_url}" },
        { "name" : "API_BASE_URL_ISSUER_URI", "value" : "https://${module.cognito.issuer_domain}" },
        { "name" : "API_BASE_URL_OAUTH", "value" : "https://${module.cognito.auth_domain}" },
        { "name" : "API_BASE_URL_HMRC", "value" : "https://a0519c3b-e75b-41aa-b79d-7bb41871ec62.mock.pstmn.io" },
        { "name" : "API_BASE_URL_DATA_RECEIVER", "value" : "http://gdx-data-share-poc:8080" },
        { "name" : "API_BASE_URL_EVENT_DATA_RETRIEVAL", "value" : "http://localhost:8080" },
        { "name" : "API_BASE_S3_INGRESS", "value" : module.ingress.name },
        { "name" : "API_BASE_S3_INGRESS_ARCHIVE", "value" : module.ingress_archive.name },

        { "name" : "HMPPS_SQS_TOPICS_EVENT_ACCESSKEYID", "value" : module.sns.access_key_id },
        { "name" : "HMPPS_SQS_TOPICS_EVENT_SECRETACCESSKEY", "value" : module.sns.access_key_secret },
        { "name" : "HMPPS_SQS_TOPICS_EVENT_ARN", "value" : module.sns.sns_topic_arn },

        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_QUEUEACCESSKEYID", "value" : module.data_processor_queue.queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_QUEUESECRETACCESSKEY", "value" : module.data_processor_queue.queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_QUEUENAME", "value" : module.data_processor_queue.queue_name },
        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_DLQACCESSKEYID", "value" : module.data_processor_queue.dead_letter_queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_DLQSECRETACCESSKEY", "value" : module.data_processor_queue.dead_letter_queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_DATAPROCESSOR_DLQNAME", "value" : module.data_processor_queue.dead_letter_queue_name },

        { "name" : "HMPPS_SQS_QUEUES_AUDIT_QUEUEACCESSKEYID", "value" : module.audit_queue.queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_AUDIT_QUEUESECRETACCESSKEY", "value" : module.audit_queue.queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_AUDIT_QUEUENAME", "value" : module.audit_queue.queue_name },
        { "name" : "HMPPS_SQS_QUEUES_AUDIT_DLQACCESSKEYID", "value" : module.audit_queue.dead_letter_queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_AUDIT_DLQSECRETACCESSKEY", "value" : module.audit_queue.dead_letter_queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_AUDIT_DLQNAME", "value" : module.audit_queue.dead_letter_queue_name },

        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_QUEUEACCESSKEYID", "value" : module.outbound_adaptor_queue.queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_QUEUESECRETACCESSKEY", "value" : module.outbound_adaptor_queue.queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_QUEUENAME", "value" : module.outbound_adaptor_queue.queue_name },
        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_DLQACCESSKEYID", "value" : module.outbound_adaptor_queue.dead_letter_queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_DLQSECRETACCESSKEY", "value" : module.outbound_adaptor_queue.dead_letter_queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_ADAPTOR_DLQNAME", "value" : module.outbound_adaptor_queue.dead_letter_queue_name },

        { "name" : "HMPPS_SQS_QUEUES_ODG_QUEUEACCESSKEYID", "value" : module.other_department_queue.queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_ODG_QUEUESECRETACCESSKEY", "value" : module.other_department_queue.queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_ODG_QUEUENAME", "value" : module.other_department_queue.queue_name },
        { "name" : "HMPPS_SQS_QUEUES_ODG_DLQACCESSKEYID", "value" : module.other_department_queue.dead_letter_queue_access_key_id },
        { "name" : "HMPPS_SQS_QUEUES_ODG_DLQSECRETACCESSKEY", "value" : module.other_department_queue.dead_letter_queue_access_key_secret },
        { "name" : "HMPPS_SQS_QUEUES_ODG_DLQNAME", "value" : module.other_department_queue.dead_letter_queue_name },

        { "name" : "SPRING_FLYWAY_URL", "value" : "jdbc:${local.rds_db_url}" },
        { "name" : "SPRING_FLYWAY_USER", "value" : aws_rds_cluster.rds_postgres_cluster.master_username },
        { "name" : "SPRING_FLYWAY_PASSWORD", "value" : random_password.rds_password.result },
        { "name" : "SPRING_R2DBC_URL", "value" : "r2dbc:${local.rds_db_url}" },
        { "name" : "SPRING_R2DBC_USERNAME", "value" : aws_rds_cluster.rds_postgres_cluster.master_username },
        { "name" : "SPRING_R2DBC_PASSWORD", "value" : random_password.rds_password.result },

        { "name" : "LEGACY_INBOUND_API_CLIENT_ID", "value" : module.cognito.legacy_inbound_client_id },
        { "name" : "LEGACY_INBOUND_API_CLIENT_SECRET", "value" : module.cognito.legacy_inbound_client_secret },
        { "name" : "LEGACY_OUTBOUND_API_CLIENT_ID", "value" : module.cognito.legacy_outbound_client_id },
        { "name" : "LEGACY_OUTBOUND_API_CLIENT_SECRET", "value" : module.cognito.legacy_outbound_client_secret },

        { "name" : "SPRINGDOC_SWAGGER_UI_OAUTH2_REDIRECT_URL", "value" : "https://${aws_cloudfront_distribution.gdx_data_share_poc.domain_name}/webjars/swagger-ui/oauth2-redirect.html" },
      ]
      logConfiguration : {
        logDriver : "awslogs",
        options : {
          awslogs-group : aws_cloudwatch_log_group.ecs_logs.name,
          awslogs-region : var.region,
          awslogs-stream-prefix : "gdx-data-share-poc",
          awslogs-create-group : "true"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "gdx_data_share_poc" {
  name                              = "${var.environment}-gdx-data-share-poc"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.gdx_data_share_poc.arn
  launch_type                       = "FARGATE"
  desired_count                     = 2
  health_check_grace_period_seconds = 120

  lifecycle {
    ignore_changes = [
      desired_count,
      task_definition,
      load_balancer
    ]
  }

  deployment_controller {
    type = "CODE_DEPLOY"
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.green.arn
    container_name   = "${var.environment}-gdx-data-share-poc"
    container_port   = 8080
  }

  network_configuration {
    security_groups = [aws_security_group.ecs_tasks.id]
    subnets         = module.vpc.private_subnet_ids
  }

  depends_on = [
    aws_security_group.ecs_tasks,
    aws_lb_target_group.green,
    aws_lb_listener.listener-http
  ]
}
