#tfsec:ignore:aws-ecr-enforce-immutable-repository
resource "aws_ecr_repository" "gdx_data_share_poc" {
  name = "gdx-data-share-poc"

  image_scanning_configuration {
    scan_on_push = true
  }
  encryption_configuration {
    encryption_type = "KMS"
    kms_key         = aws_kms_key.gdx_data_share_poc_key.arn
  }
}

resource "aws_kms_key" "gdx_data_share_poc_key" {
  description         = "ECR repository encryption key"
  enable_key_rotation = true
}

resource "aws_ecr_registry_scanning_configuration" "ecr_scanning_configuration" {
  scan_type = "BASIC"

  rule {
    scan_frequency = "SCAN_ON_PUSH"
    repository_filter {
      filter      = aws_ecr_repository.gdx_data_share_poc.name
      filter_type = "WILDCARD"
    }
  }
}

resource "aws_ecr_pull_through_cache_rule" "example" {
  ecr_repository_prefix = "quay"
  upstream_registry_url = "quay.io"
}