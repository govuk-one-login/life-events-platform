locals {
  auth_base_url = "https://${aws_cognito_user_pool.pool.domain}.auth.${var.region}.amazoncognito.com"
}

resource "aws_ecs_cluster" "grafana" {
  name = "grafana"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_task_definition" "grafana" {
  family                   = "grafana"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  memory                   = 512
  cpu                      = 256
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "grafana",
      image = "grafana/grafana-enterprise:9.4.1",
      portMappings = [{
        containerPort : 3000,
        hostPort : 3000,
        protocol : "tcp",
      }],
      environment = [
        { "name" : "AWS_SDK_LOAD_CONFIG", "value" : "true" },
        { "name" : "GF_AUTH_SIGV4_AUTH_ENABLED", "value" : "true" },

        { "name" : "GF_SERVER_DOMAIN", "value" : aws_cloudfront_distribution.grafana.domain_name },
        { "name" : "GF_SERVER_ROOT_URL", "value" : "https://${aws_cloudfront_distribution.grafana.domain_name}" },
        { "name" : "GF_SESSION_COOKIE_SECURE", "value" : "true" },
        { "name" : "GF_SESSION_COOKIE_SAMESITE", "value" : "lax" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_ENABLED", "value" : "true" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_NAME", "value" : "Cognito" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_ALLOW_SIGN_UP", "value" : "true" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_CLIENT_ID", "value" : aws_cognito_user_pool_client.grafana.id },
        { "name" : "GF_AUTH_GENERIC_OAUTH_CLIENT_SECRET", "value" : aws_cognito_user_pool_client.grafana.client_secret },
        { "name" : "GF_AUTH_GENERIC_OAUTH_SCOPES", "value" : "email profile aws.cognito.signin.user.admin openid" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_AUTH_URL", "value" : "${local.auth_base_url}/oauth2/authorize" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_TOKEN_URL", "value" : "${local.auth_base_url}/oauth2/token" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_API_URL", "value" : "${local.auth_base_url}/oauth2/userInfo" },
        { "name" : "GF_AUTH_SIGNOUT_REDIRECT_URL", "value" : "${local.auth_base_url}/logout?client_id=${aws_cognito_user_pool_client.grafana.id}&logout_uri=https://${aws_cloudfront_distribution.grafana.domain_name}/login" },
        { "name" : "GF_AUTH_GENERIC_OAUTH_ROLE_ATTRIBUTE_PATH", "value" : "(\"cognito:groups\" | contains([*], 'Admin') && 'Admin' || 'Viewer')" }
      ],
      logConfiguration : {
        logDriver : "awslogs",
        options : {
          awslogs-group : aws_cloudwatch_log_group.ecs_logs.name,
          awslogs-region : var.region,
          awslogs-stream-prefix : "gdx-data-share-poc",
          awslogs-create-group : "true"
        }
      },
      mountPoints : [
        {
          "sourceVolume" : "grafana",
          "containerPath" : "/var/lib/grafana",
          "readOnly" : false,
        }
      ]
      healthCheck : {
        command : ["CMD-SHELL", "wget --tries=1 --spider http://localhost:3000/api/health"],
        startPeriod : 300,
        interval : 30,
        retries : 3,
        timeout : 5
      },
      essential : true,
      cpu : 0,
    }
  ])
  volume {
    name = "grafana"
    efs_volume_configuration {
      file_system_id          = aws_efs_file_system.grafana.id
      transit_encryption      = "ENABLED"
      transit_encryption_port = 2049
      authorization_config {
        access_point_id = aws_efs_access_point.grafana.id
        iam             = "ENABLED"
      }
    }
  }
}

resource "aws_ecs_service" "grafana" {
  name                              = "grafana"
  cluster                           = aws_ecs_cluster.grafana.id
  task_definition                   = aws_ecs_task_definition.grafana.arn
  launch_type                       = "FARGATE"
  desired_count                     = 1
  health_check_grace_period_seconds = 120

  load_balancer {
    target_group_arn = aws_lb_target_group.grafana.arn
    container_name   = "grafana"
    container_port   = 3000
  }

  network_configuration {
    security_groups = [aws_security_group.ecs_task.id]
    subnets         = var.private_subnet_ids
  }

  depends_on = [
    aws_security_group.ecs_task,
    aws_lb_target_group.grafana,
    aws_lb_listener.listener_http
  ]
}
