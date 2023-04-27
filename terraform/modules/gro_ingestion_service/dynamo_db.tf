#tfsec:ignore:aws-dynamodb-enable-recovery
resource "aws_dynamodb_table" "gro_ingestion" {
  name             = "${var.environment}_gro_ingestion"
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "hash"
  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"

  attribute {
    name = "hash"
    type = "S"
  }

  server_side_encryption {
    enabled     = true
    kms_key_arn = aws_kms_key.gro_ingestion.arn
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
}
