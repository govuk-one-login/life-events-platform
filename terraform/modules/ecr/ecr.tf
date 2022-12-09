resource "aws_ecr_repository" "ecr_repo" {
  name = "ecr_repo"
}

resource "aws_ecr_registry_scanning_configuration" "ecr_scanning_configuration" {
  scan_type = "BASIC"

  rule {
    scan_frequency = "SCAN_ON_PUSH"
    repository_filter {
      filter      = "ecr_repo"
      filter_type = "WILDCARD"
    }
  }
}