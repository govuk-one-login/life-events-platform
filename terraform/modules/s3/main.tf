resource "aws_s3_bucket" "bucket" {
  bucket = "${var.environment}-${var.name}-gdx-data-share-poc"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "bucket_acl" {
  bucket = aws_s3_bucket.bucket.id
  acl    = "private"
}

resource "aws_s3_bucket_versioning" "bucket" {
  bucket = aws_s3_bucket.bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_kms_key" "bucket" {
  enable_key_rotation = true
  description         = "Key used to encrypt state bucket"
}

resource "aws_kms_alias" "bucket_alias" {
  name          = "alias/${var.environment}/${var.name}-bucket-key"
  target_key_id = aws_kms_key.bucket.arn
}

resource "aws_s3_bucket_server_side_encryption_configuration" "bucket" {
  bucket = aws_s3_bucket.bucket.bucket

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.bucket.arn
      sse_algorithm     = "aws:kms"
    }
  }
}

resource "aws_s3_bucket_logging" "bucket_logging" {
  bucket = aws_s3_bucket.bucket.id

  target_bucket = aws_s3_bucket.log_bucket.id
  target_prefix = "log/"
}

resource "aws_s3_bucket_lifecycle_configuration" "bucket_lifecycle" {
  count  = var.expiration_days == null ? 0 : 1
  bucket = aws_s3_bucket.bucket.id

  rule {
    id = "${aws_s3_bucket.bucket.bucket}-lifecycle-rule"
    expiration {
      days = var.expiration_days
    }
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "bucket" {
  bucket = aws_s3_bucket.bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

data "aws_iam_policy_document" "deny_insecure_transport" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    actions = [
      "s3:*",
    ]

    resources = [
      aws_s3_bucket.bucket.arn,
      "${aws_s3_bucket.bucket.arn}/*",
    ]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values = [
        "false"
      ]
    }
  }
}

resource "aws_s3_bucket_policy" "deny_insecure_transport" {
  bucket = aws_s3_bucket.bucket.id
  policy = data.aws_iam_policy_document.deny_insecure_transport.json
}
