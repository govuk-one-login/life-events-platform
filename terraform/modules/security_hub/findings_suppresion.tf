resource "aws_kms_key" "security_hub_findings" {
  description         = "Encryption key for security hub findings"
  enable_key_rotation = true

  policy = data.aws_iam_policy_document.kms_log_key_policy.json
}

resource "aws_kms_alias" "security_hub_findings" {
  name          = "alias/${var.environment}/security-hub-findings-key"
  target_key_id = aws_kms_key.security_hub_findings.arn
}

data "aws_iam_policy_document" "kms_log_key_policy" {
  statement {
    effect = "Allow"
    sid    = "Enable IAM User Permissions"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${var.account_id}:root"]
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["logs.${var.region}.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt*",
      "kms:Decrypt*",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:Describe*"
    ]
    resources = ["*"]
  }
}

resource "aws_dynamodb_table" "suppressor_dynamodb_table" {
  name             = "securityhub-suppression-list"
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "title"
  stream_enabled   = true
  stream_view_type = "KEYS_ONLY"

  attribute {
    name = "title"
    type = "S"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled     = true
    kms_key_arn = aws_kms_key.security_hub_findings.arn
  }
}

resource "aws_cloudwatch_event_rule" "securityhub_events_suppressor_failed_events" {
  name        = "securityhub-events-suppressor"
  description = "EventBridge Rule that detects Security Hub events with compliance status as not successful or the event has vulnerabilities as it is an Inspector event, and workflow status as new or notified"

  event_pattern = <<EOF
{
  "source": ["aws.securityhub"],
  "detail-type": ["Security Hub Findings - Imported"],
  "detail": {
    "findings": {
      "Workflow": {
        "Status": ["NEW", "NOTIFIED"]
      }
    }
  }
}
EOF
}

resource "aws_cloudwatch_event_target" "lambda_securityhub_events_suppressor" {
  arn  = module.lambda_function_suppressor.lambda_function_arn
  rule = aws_cloudwatch_event_rule.securityhub_events_suppressor_failed_events.name
}

resource "aws_lambda_permission" "allow_eventbridge_to_invoke_suppressor_lambda" {
  action        = "lambda:InvokeFunction"
  function_name = module.lambda_function_suppressor.lambda_function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.securityhub_events_suppressor_failed_events.arn
}

resource "aws_lambda_event_source_mapping" "lambda_securityhub_streams_mapping" {
  event_source_arn  = aws_dynamodb_table.suppressor_dynamodb_table.stream_arn
  function_name     = module.lambda_function_suppressor.lambda_function_name
  starting_position = "LATEST"
}
