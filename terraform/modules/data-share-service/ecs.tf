resource "aws_ecs_cluster" "main" {
  name = var.environment

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

locals {
  rds_db_url = "jdbc:aws-wrapper:postgresql://${aws_rds_cluster.rds_postgres_cluster.endpoint}:${aws_rds_cluster.rds_postgres_cluster.port}/${aws_rds_cluster.rds_postgres_cluster.database_name}?wrapperPlugins=iam&sslmode=require"
}

resource "aws_ecs_task_definition" "gdx_data_share_poc" {
  family                   = "${var.environment}-gdx-data-share-poc"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  memory                   = 2048
  cpu                      = 1024
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "${var.environment}-gdx-data-share-poc",
      image        = "${var.ecr_url}/gdx-data-share-poc:${var.environment}",
      portMappings = [{ "containerPort" : 8080, "hostPort" : 8080 }],
      environment = [
        { "name" : "ENVIRONMENT", "value" : var.environment },

        { "name" : "SERVER_PORT", "value" : "8080" },

        { "name" : "API_BASE_URL_LEV", "value" : "https://${var.lev_url}" },
        { "name" : "API_BASE_URL_ISSUER_URI", "value" : "https://${module.cognito.issuer_domain}" },
        { "name" : "API_BASE_URL_OAUTH", "value" : "https://${module.cognito.auth_domain}" },

        { "name" : "API_BASE_PRISONER_EVENT_ENABLED", "value" : var.prisoner_event_enabled },
        { "name" : "API_BASE_URL_PRISONER_SEARCH", "value" : var.prisoner_search_url },
        { "name" : "API_BASE_URL_HMPPS_AUTH", "value" : var.hmpps_auth_url },
        { "name" : "API_BASE_PRISONER_SEARCH_API_CLIENT_ID", "value" : aws_ssm_parameter.prisoner_search_api_client_id },
        { "name" : "API_BASE_PRISONER_SEARCH_API_CLIENT_SECRET", "value" : aws_ssm_parameter.prisoner_search_api_client_secret },

        { "name" : "COGNITO_USER_POOL_ID", "value" : module.cognito.user_pool_id },
        { "name" : "COGNITO_ACQUIRER_SCOPE", "value" : module.cognito.acquirer_scope },
        { "name" : "COGNITO_SUPPLIER_SCOPE", "value" : module.cognito.supplier_scope },
        { "name" : "COGNITO_ADMIN_SCOPE", "value" : module.cognito.admin_scope },

        { "name" : "SPRING_DATASOURCE_URL", "value" : local.rds_db_url },
        { "name" : "SPRING_DATASOURCE_USERNAME", "value" : var.db_username },

        { "name" : "METRICS_CLOUDWATCH_NAMESPACE", "value" : "${var.environment}-gdx" },

        { "name" : "SQS_QUEUES_DATAPROCESSOR_QUEUENAME", "value" : module.data_processor_queue.queue_name },
        { "name" : "SQS_QUEUES_DATAPROCESSOR_DLQNAME", "value" : module.data_processor_queue.dead_letter_queue_name },

        { "name" : "SQS_QUEUES_PRISONEREVENT_ENABLED", "value" : var.prisoner_event_enabled },
        { "name" : "SQS_QUEUES_PRISONEREVENT_QUEUENAME", "value" : aws_ssm_parameter.prisoner_event_queue_name },
        { "name" : "SQS_QUEUES_PRISONEREVENT_DLQNAME", "value" : aws_ssm_parameter.prisoner_event_dlq_name },
        {
          "name" : "SPRINGDOC_SWAGGER_UI_OAUTH2_REDIRECT_URL",
          "value" : "https://${aws_cloudfront_distribution.gdx_data_share_poc.domain_name}/webjars/swagger-ui/oauth2-redirect.html"
        },
      ]
      logConfiguration : {
        logDriver : "awslogs",
        options : {
          awslogs-group : aws_cloudwatch_log_group.ecs_logs.name,
          awslogs-region : var.region,
          awslogs-stream-prefix : "gdx-data-share-poc",
          awslogs-create-group : "true"
        }
      },
      healthCheck : {
        command : ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"],
        startPeriod : 300,
        interval : 30,
        retries : 3,
        timeout : 5
      },
      mountPoints : [],
      volumesFrom : [],
      essential : true,
      cpu : 0,
    },
    {
      name : "xray-daemon",
      image : "amazon/aws-xray-daemon"
      cpu : 32,
      memoryReservation : 256,
      portMappings : [
        {
          containerPort : 2000,
          protocol : "udp"
        }
      ]
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
