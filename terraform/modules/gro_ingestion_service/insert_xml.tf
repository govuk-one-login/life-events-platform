resource "aws_iam_role" "insert_xml_lambda" {
  name               = "${var.environment}-gro-ingestion-lambda-function-insert-xml"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_policy.json
}

# Trivy gives a false positive for iam access to the contents of an S3 bucket
# https://github.com/aquasecurity/trivy/issues/5089
#tfsec:ignore:aws-iam-no-policy-wildcards
data "aws_iam_policy_document" "insert_xml_lambda" {
  statement {
    sid = "LambdaKMSAccess"
    actions = [
      "kms:Decrypt",
      "kms:Encrypt",
      "kms:GenerateDataKey",
      "kms:GenerateDataKeyPair",
      "kms:ReEncryptFrom",
      "kms:ReEncryptTo"
    ]
    effect = "Allow"
    resources = [
      aws_kms_key.gro_ingestion.arn
    ]
  }

  statement {
    sid = "S3PutItem"
    actions = [
      "s3:PutObject",
      "s3:PutObjectAcl",
      "s3:GetObjectAcl",
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]
    resources = [
      module.gro_bucket.arn,
      "${module.gro_bucket.arn}/*",
    ]
  }

  statement {
    sid = "S3KMSPolicy"
    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey"
    ]
    resources = [
      module.gro_bucket.kms_arn
    ]
  }
}

resource "aws_iam_policy" "insert_xml_lambda" {
  name   = "${var.environment}-gro-ingestion-lambda-function-insert-xml"
  policy = data.aws_iam_policy_document.insert_xml_lambda.json
}

resource "aws_iam_role_policy_attachment" "insert_xml_lambda" {
  policy_arn = aws_iam_policy.insert_xml_lambda.arn
  role       = aws_iam_role.insert_xml_lambda.name
}

resource "aws_iam_role_policy_attachment" "insert_xml_lambda_xray_access" {
  policy_arn = data.aws_iam_policy.xray_access.arn
  role       = aws_iam_role.insert_xml_lambda.name
}

resource "aws_iam_role_policy_attachment" "insert_xml_lambda_logs_access" {
  policy_arn = aws_iam_policy.log_policy.arn
  role       = aws_iam_role.insert_xml_lambda.name
}

resource "aws_cloudwatch_log_group" "insert_xml_log" {
  name              = "/aws/lambda/${aws_lambda_function.insert_xml_lambda.function_name}"
  retention_in_days = var.cloudwatch_retention_period

  kms_key_id = aws_kms_key.gro_ingestion.arn
}

resource "aws_lambda_function" "insert_xml_lambda" {
  filename      = data.archive_file.lambda_function_source.output_path
  function_name = "${var.environment}-gro-ingestion-lambda-function-insert-xml"

  handler       = "index.handler"
  runtime       = local.lambda_runtime
  role          = aws_iam_role.insert_xml_lambda.arn
  timeout       = 10
  architectures = ["arm64"]

  source_code_hash = data.archive_file.lambda_function_source.output_sha

  environment {
    variables = {
      "FUNCTION_NAME"  = "insertXml",
      "S3_BUCKET_NAME" = module.gro_bucket.id
    }
  }
  tracing_config {
    mode = "Active"
  }
}

resource "aws_cloudwatch_event_rule" "schedule" {
  name                = "${var.environment}GROInsertXmlLambdaSchedule"
  description         = "Schedule for Lambda Function to insert mock GRO files into S3"
  schedule_expression = var.insert_xml_lambda_schedule
}

resource "aws_cloudwatch_event_target" "schedule_lambda" {
  rule      = aws_cloudwatch_event_rule.schedule.name
  target_id = "${var.environment}_gro_insert_xml_lambda"
  arn       = aws_lambda_function.insert_xml_lambda.arn
}

resource "aws_lambda_permission" "allow_events_bridge_to_run_lambda" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.insert_xml_lambda.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.schedule.arn
}
