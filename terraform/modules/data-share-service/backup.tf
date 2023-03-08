module "cross_region_vault" {
  source    = "../backup_vault"
  providers = { aws = aws.eu-west-1 }

  environment = var.environment
  name        = "cross-region"
}

module "main_vault" {
  source = "../backup_vault"

  environment = var.environment
  name        = "main"
}

data "aws_iam_policy_document" "backup_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["backup.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}
resource "aws_iam_role" "backup_role" {
  name               = "${var.environment}-backup-role"
  assume_role_policy = data.aws_iam_policy_document.backup_assume_role.json
}

data "aws_iam_policy" "backup_service_policy" {
  name = "AWSBackupServiceRolePolicyForBackup"
}

resource "aws_iam_role_policy_attachment" "backup_service_policy" {
  policy_arn = data.aws_iam_policy.backup_service_policy.arn
  role       = aws_iam_role.backup_role.name
}

resource "aws_backup_region_settings" "region_settings" {
  resource_type_opt_in_preference = {
    "Aurora"          = true
    "CloudFormation"  = false
    "DocumentDB"      = false
    "DynamoDB"        = true
    "EBS"             = true
    "EC2"             = true
    "EFS"             = true
    "FSx"             = false
    "Neptune"         = false
    "RDS"             = true
    "Redshift"        = false
    "S3"              = false
    "Storage Gateway" = true
    "VirtualMachine"  = false
  }
}

resource "aws_backup_plan" "rds" {
  name = "${var.environment}-rds-backup-plan"

  rule {
    rule_name         = "${var.environment}-rds-backup-rule"
    target_vault_name = module.main_vault.name
    schedule          = "cron(0 4 * * ? *)"

    lifecycle {
      delete_after = 14
    }

    copy_action {
      destination_vault_arn = module.cross_region_vault.arn

      lifecycle {
        delete_after = 14
      }
    }
  }
}

resource "aws_backup_selection" "rds_backup" {
  iam_role_arn = aws_iam_role.backup_role.arn
  name         = "${var.environment}-rds-backup"
  plan_id      = aws_backup_plan.rds.id

  resources = [
    aws_rds_cluster.rds_postgres_cluster.arn
  ]
}
