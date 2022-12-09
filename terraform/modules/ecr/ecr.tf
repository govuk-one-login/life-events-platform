resource "aws_ecr_repository" "ecr_repo" {
  name = "ecr-repo"

  image_scanning_configuration {
    scan_on_push = true
  }
  encryption_configuration {
    encryption_type = "KMS"
    kms_key         = aws_kms_key.ecr_repo_key.arn
  }
}

resource "aws_kms_key" "ecr_repo_key" {
  description         = "ECR repository encryption key"
  enable_key_rotation = true
}

resource "aws_ecr_registry_scanning_configuration" "ecr_scanning_configuration" {
  scan_type = "BASIC"

  rule {
    scan_frequency = "SCAN_ON_PUSH"
    repository_filter {
      filter      = "ecr-repo"
      filter_type = "WILDCARD"
    }
  }
}