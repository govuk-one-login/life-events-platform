#tfsec:ignore:aws-dynamodb-enable-recovery
resource "aws_dynamodb_table" "gor_ingestion" {
  name             = "${var.environment}_gro_ingestion"
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "hash"
  stream_enabled   = true
  stream_view_type = "KEYS_ONLY"

  attribute {
    name = "hash"
    type = "S"
  }

  attribute {
    name = "FirstForename"
    type = "S"
  }

  attribute {
    name = "Surname"
    type = "S"
  }

  attribute {
    name = "MaidenSurname"
    type = "S"
  }

  attribute {
    name = "Sex"
    type = "S"
  }

  attribute {
    name = "DateOfDeath"
    type = "S"
  }

  attribute {
    name = "DateOfBirth"
    type = "S"
  }

  attribute {
    name = "AddressLine1"
    type = "S"
  }

  attribute {
    name = "AddressLine2"
    type = "S"
  }

  attribute {
    name = "AddressLine3"
    type = "S"
  }

  attribute {
    name = "AddressLine4"
    type = "S"
  }

  attribute {
    name = "Postcode"
    type = "S"
  }

  attribute {
    name = "VerificationLevelTypes"
    type = "N"
  }

  attribute {
    name = "PartialMonthOfDeath"
    type = "N"
  }

  attribute {
    name = "PartialYearOfDeath"
    type = "N"
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
