locals {
  lambda_runtime = "nodejs18.x"
}

data "archive_file" "typescript_source" {
  type        = "zip"
  source_dir  = "${path.module}/lambdas/src"
  output_path = "${path.module}/zip/typescript-source.zip"
}

data "archive_file" "lambda_function_source" {
  type        = "zip"
  source_dir  = "${path.module}/lambdas/dist"
  output_path = "${path.module}/zip/lambda-function-source.zip"
  depends_on  = [null_resource.lambda_function_source_builder]
}

resource "null_resource" "lambda_function_source_builder" {
  provisioner "local-exec" {
    working_dir = "${path.module}/lambdas"
    command     = <<EOT
      npm ci
      npm run build
    EOT
  }
  triggers = {
    source_code_md5 = data.archive_file.typescript_source.output_md5
    file_dist       = fileexists("${path.module}/lambdas/dist/index.js") ? "${path.module}/lambdas/dist/index.js" : timestamp()
  }
}

data "aws_iam_policy_document" "lambda_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy" "xray_access" {
  name = "AWSXRayDaemonWriteAccess"
}
