resource "aws_dynamodb_table" "gor_ingestion" {
  name             = "gro_ingestion"
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "hash"
  stream_enabled   = true
  stream_view_type = "KEYS_ONLY"

  attribute {
    name = "hash"
    type = "S"
  }

  server_side_encryption {
    enabled     = true
    kms_key_arn = aws_kms_key.gor_ingestion.arn
  }
}

resource "aws_kms_key" "gor_ingestion" {
  description         = "Encryption key for GRO ingestion"
  enable_key_rotation = true
}

resource "aws_kms_alias" "gor_ingestion" {
  name          = "alias/${var.environment}/gro-ingestion-key"
  target_key_id = aws_kms_key.gor_ingestion.arn
}
