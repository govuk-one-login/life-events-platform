resource "aws_ecs_cluster" "main" {
  name = var.environment

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

locals {
  rds_db_url = "jdbc:aws-wrapper:postgresql://${aws_rds_cluster.rds_postgres_cluster.endpoint}:${aws_rds_cluster.rds_postgres_cluster.port}/${aws_rds_cluster.rds_postgres_cluster.database_name}?wrapperPlugins=iam,failover&sslmode=verify-full"
}

resource "aws_ecs_task_definition" "gdx_data_share_poc" {
  family                   = "${var.environment}-gdx-data-share-poc"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  memory                   = 2048
  cpu                      = 1024
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "${var.environment}-gdx-data-share-poc",
      image = "${var.ecr_url}/gdx-data-share-poc:${var.environment}",
      portMappings = [
        {
          containerPort : 8080,
          hostPort : 8080,
          protocol : "tcp",
        }
      ],
      environment = [
        { "name" : "ENVIRONMENT", "value" : var.environment },

        { "name" : "SERVER_PORT", "value" : "8080" },

        { "name" : "API_BASE_URL_LEV", "value" : "https://${var.lev_url}" },
        { "name" : "API_BASE_URL_ISSUER_URI", "value" : "https://${module.cognito.issuer_domain}" },
        { "name" : "API_BASE_URL_OAUTH", "value" : "https://${module.cognito.auth_domain}" },

        { "name" : "API_BASE_PRISONER_EVENT_ENABLED", "value" : var.prisoner_event_enabled },
        { "name" : "API_BASE_URL_PRISONER_SEARCH", "value" : var.prisoner_search_url },
        { "name" : "API_BASE_URL_HMPPS_AUTH", "value" : var.hmpps_auth_url },

        { "name" : "COGNITO_USER_POOL_ID", "value" : module.cognito.user_pool_id },
        { "name" : "COGNITO_ACQUIRER_SCOPE", "value" : module.cognito.acquirer_scope },
        { "name" : "COGNITO_SUPPLIER_SCOPE", "value" : module.cognito.supplier_scope },
        { "name" : "COGNITO_ADMIN_SCOPE", "value" : module.cognito.admin_scope },

        { "name" : "PROMETHEUS_USER_NAME", "value" : random_password.prometheus_username.result },
        { "name" : "PROMETHEUS_USER_PASSWORD", "value" : random_password.prometheus_password.result },

        { "name" : "SPRING_DATASOURCE_URL", "value" : local.rds_db_url },
        { "name" : "SPRING_DATASOURCE_USERNAME", "value" : var.db_username },

        { "name" : "SQS_QUEUES_SUPPLIEREVENT_QUEUENAME", "value" : module.supplier_event_queue.queue_name },
        { "name" : "SQS_QUEUES_SUPPLIEREVENT_DLQNAME", "value" : module.supplier_event_queue.dead_letter_queue_name },

        { "name" : "SQS_QUEUES_ACQUIREREVENT_QUEUENAME", "value" : module.acquirer_event_queue.queue_name },
        { "name" : "SQS_QUEUES_ACQUIREREVENT_DLQNAME", "value" : module.acquirer_event_queue.dead_letter_queue_name },

        { "name" : "SQS_QUEUES_PRISONEREVENT_ENABLED", "value" : var.prisoner_event_enabled },
        {
          "name" : "SPRINGDOC_SWAGGER_UI_OAUTH2_REDIRECT_URL",
          "value" : "${local.gdx_api_base_url}/swagger-ui/oauth2-redirect.html"
        },
        { "name" : "AWS_XRAY_CONTEXT_MISSING", "value" : "IGNORE_ERROR" },
        { "name" : "ADMIN_ACTION_ALERT_SNS_TOPIC_ARN", "value" : module.sns_admin_alerts.topic_arn },

        { "name" : "DELETE_EVENT_LAMBDA_FUNCTION_NAME", "value" : var.delete_event_function_name },
        { "name" : "ENRICH_EVENT_LAMBDA_FUNCTION_NAME", "value" : var.enrich_event_function_name },
      ]
      secrets = [
        {
          "name" : "API_BASE_PRISONER_SEARCH_API_CLIENT_ID",
          "valueFrom" : aws_ssm_parameter.prisoner_search_api_client_id.arn
        },
        {
          "name" : "API_BASE_PRISONER_SEARCH_API_CLIENT_SECRET",
          "valueFrom" : aws_ssm_parameter.prisoner_search_api_client_secret.arn
        },
        {
          "name" : "SQS_QUEUES_PRISONEREVENT_QUEUENAME", "valueFrom" : aws_ssm_parameter.prisoner_event_queue_name.arn
        },
        { "name" : "SQS_QUEUES_PRISONEREVENT_DLQNAME", "valueFrom" : aws_ssm_parameter.prisoner_event_dlq_name.arn },
        {
          "name" : "SQS_QUEUES_PRISONEREVENT_AWSACCOUNTID",
          "valueFrom" : aws_ssm_parameter.prisoner_event_aws_account_id.arn
        },
        { "name" : "API_BASE_LEV_API_CLIENT_NAME", "valueFrom" : aws_ssm_parameter.lev_api_client_name.arn },
        { "name" : "API_BASE_LEV_API_CLIENT_USER", "valueFrom" : aws_ssm_parameter.lev_api_client_user.arn },
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
        command : ["CMD-SHELL", "wget --tries=1 --spider http://localhost:8080/health"],
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
      image : "${var.ecr_url}/ecr-public/xray/aws-xray-daemon:3.3.6",
      cpu : 32,
      memoryReservation : 256,
      portMappings : [
        {
          containerPort : 2000,
          hostPort : 2000,
          protocol : "udp"
        }
      ],
      mountPoints : [],
      volumesFrom : [],
      essential : true,
      environment : [],
    },
    {
      name : "adot-collector",
      image : "${var.ecr_url}/prometheus-adot:f9215a1fd3be16144552e1e9dbc9688125ba3f7f2d858ee597885a6f93a10904",
      essential : true,
      logConfiguration : {
        logDriver : "awslogs",
        options : {
          awslogs-group : aws_cloudwatch_log_group.ecs_adot_logs.name,
          awslogs-region : var.region,
          awslogs-stream-prefix : "ecs",
          awslogs-create-group : "True"
        }
      },
      environment = [
        { "name" : "PROMETHEUS_USERNAME", "value" : random_password.prometheus_username.result },
        { "name" : "PROMETHEUS_PASSWORD", "value" : random_password.prometheus_password.result },
        {
          "name" : "AWS_PROMETHEUS_ENDPOINT",
          "value" : "${aws_prometheus_workspace.prometheus.prometheus_endpoint}api/v1/remote_write"
        },
        { "name" : "AWS_REGION", "value" : var.region },
      ],
    },
  ])
}

resource "aws_ecs_service" "gdx_data_share_poc" {
  name                              = "${var.environment}-gdx-data-share-poc"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.gdx_data_share_poc.arn
  launch_type                       = "FARGATE"
  platform_version                  = "1.4.0"
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
    aws_lb_listener.listener_https
  ]
}
