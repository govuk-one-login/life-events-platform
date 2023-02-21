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
      image = "grafana/grafana-enterprise:9.3.6",
      portMappings = [{
        containerPort : 3000,
        hostPort : 3000,
        protocol : "tcp",
      }],
      environment = [
        { "name" : "AWS_SDK_LOAD_CONFIG", "value" : "true" },
        { "name" : "GF_AUTH_SIGV4_AUTH_ENABLED", "value" : "true" },
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
