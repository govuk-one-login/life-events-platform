output "id" {
  value = aws_s3_bucket.bucket.id
}
output "name" {
  value = aws_s3_bucket.bucket.bucket
}
output "arn" {
  value = aws_s3_bucket.bucket.arn
}
output "objects_arn" {
  value = "${aws_s3_bucket.bucket.arn}/*"
}
output "kms_arn" {
  value = aws_kms_key.bucket.arn
}
