output "id" {
  value = aws_s3_bucket.bucket.id
}
output "arn" {
  value = aws_s3_bucket.bucket.arn
}
output "objects_arn" {
  value = "${aws_s3_bucket.bucket.arn}/*"
}
output "kms_arn" {
  value = var.use_kms ? aws_kms_key.bucket[0].arn : ""
}
