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
