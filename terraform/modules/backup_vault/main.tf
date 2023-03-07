terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

resource "aws_kms_key" "vault_key" {
  description         = "Encryption key for ${var.name} backup vault in ${var.environment}}"
  enable_key_rotation = true
}

resource "aws_kms_alias" "vault_key_alias" {
  name          = "alias/${var.environment}/${var.name}-backup-vault-key"
  target_key_id = aws_kms_key.vault_key.arn
}

resource "aws_backup_vault" "vault" {
  name        = "${var.environment}-${var.name}-backup-vault"
  kms_key_arn = aws_kms_key.vault_key.arn
}

data "aws_iam_policy_document" "vault" {
  statement {
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    actions = [
      "backup:DescribeBackupVault",
      "backup:DeleteBackupVault",
      "backup:PutBackupVaultAccessPolicy",
      "backup:DeleteBackupVaultAccessPolicy",
      "backup:GetBackupVaultAccessPolicy",
      "backup:StartBackupJob",
      "backup:GetBackupVaultNotifications",
      "backup:PutBackupVaultNotifications",
    ]

    resources = [aws_backup_vault.vault.arn]
  }
}

resource "aws_backup_vault_policy" "cross_region_vault" {
  backup_vault_name = aws_backup_vault.vault.name
  policy            = data.aws_iam_policy_document.vault.json
}
