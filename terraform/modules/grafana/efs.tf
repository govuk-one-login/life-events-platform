resource "aws_efs_file_system" "grafana" {
  encrypted = "true"
}

resource "aws_efs_mount_target" "mount" {
  count = length(var.private_subnet_ids)

  file_system_id  = aws_efs_file_system.grafana.id
  subnet_id       = var.private_subnet_ids[count.index]
  security_groups = [aws_security_group.efs.id]
}

resource "aws_efs_access_point" "grafana" {
  file_system_id = aws_efs_file_system.grafana.id
  posix_user {
    gid = 0
    uid = 0
  }

  root_directory {
    path = "/config"
    creation_info {
      owner_gid   = 0
      owner_uid   = 0
      permissions = "0700"
    }
  }
}

resource "aws_security_group" "efs" {
  name        = "grafana-efs"
  description = "For EFS, access from grafana only"
  vpc_id      = var.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "efs_ingress" {
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 2049
  to_port                  = 2049
  source_security_group_id = aws_security_group.ecs_task.id
  description              = "EFS task ingress rule, allow access from ECS"
  security_group_id        = aws_security_group.efs.id
}

resource "aws_efs_backup_policy" "automatic_backup_policy" {
  file_system_id = aws_efs_file_system.grafana.id

  backup_policy {
    status = "ENABLED"
  }
}
