locals {
  lambda_runtime = "nodejs18.x"
  functions = toset([
    for file in fileset("${path.module}/lambdas/src/functions", "*.function.ts") : lower(replace(split(".", split("/", file)[length(split("/", file)) - 1])[0], "[^a-zA-Z0-9]", "-"))
  ])
}

data "archive_file" "typescript-source" {
  type        = "zip"
  source_dir  = "${path.module}/lambdas/src"
  output_path = "zip/typescript-source.zip"
}

data "archive_file" "lambda-function-source" {
  type        = "zip"
  source_dir  = "${path.module}/lambdas/dist"
  output_path = "zip/lambda-function-source.zip"
  depends_on  = [null_resource.lambda-function-source-builder]
}

resource "null_resource" "lambda-function-source-builder" {
  provisioner "local-exec" {
    working_dir = "${path.module}/lambdas"
    command     = <<EOT
      npm i
      npm run build
    EOT
  }
  triggers = {
    lambda-function-md5 = data.archive_file.typescript-source.output_md5
    file_dist           = fileexists("${path.module}/lambdas/dist/index.js") ? "${path.module}/lambdas/dist/index.js" : timestamp()
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

resource "aws_iam_role" "lambda_role" {
  for_each = local.functions
  name     = "ServiceRoleForLambda-${each.value}"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

resource "aws_lambda_function" "lambda-function" {
  for_each      = local.functions
  filename      = data.archive_file.lambda-function-source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-${each.value}"

  handler     = "index.handler"
  runtime     = local.lambda_runtime
  role        = aws_iam_role.lambda_role[each.value].arn
  timeout     = 10

  source_code_hash = data.archive_file.lambda-function-source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME"       = each.value
    }
  }
}
