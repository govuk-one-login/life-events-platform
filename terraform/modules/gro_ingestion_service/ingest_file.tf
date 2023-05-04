resource "aws_sfn_state_machine" "stepfunction" {
  name     = "${var.environment}-gro-ingestion-stepfunction-ingest-file"
  role_arn = aws_iam_role.stepfunction.arn
  tracing_configuration {
    enabled = true
  }

  definition = <<JSON
{
  "Comment": "Ingest and delete GRO file",
  "StartAt": "Lambda SplitXml",
  "States": {
    "Lambda SplitXml": {
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke",
      "OutputPath": "$.Payload",
      "Parameters": {
        "Payload.$": "$",
        "FunctionName": "${aws_lambda_function.split_xml_lambda.arn}:$LATEST"
      },
      "Retry": [
        {
          "ErrorEquals": [
            "Lambda.ServiceException",
            "Lambda.AWSLambdaException",
            "Lambda.SdkClientException",
            "Lambda.TooManyRequestsException",
            "Lambda.Unknown"
          ],
          "IntervalSeconds": 2,
          "MaxAttempts": 6,
          "BackoffRate": 2
        }
      ],
      "Next": "Map"
    },
    "Map": {
      "Type": "Map",
      "ItemProcessor": {
        "ProcessorConfig": {
          "Mode": "DISTRIBUTED",
          "ExecutionType": "STANDARD"
        },
        "StartAt": "Lambda MapXml",
        "States": {
          "Lambda MapXml": {
            "Type": "Task",
            "Resource": "arn:aws:states:::lambda:invoke",
            "OutputPath": "$.Payload",
            "Parameters": {
              "Payload.$": "$",
              "FunctionName": "${aws_lambda_function.map_xml_lambda.arn}:$LATEST"
            },
            "Retry": [
              {
                "ErrorEquals": [
                  "Lambda.ServiceException",
                  "Lambda.AWSLambdaException",
                  "Lambda.SdkClientException",
                  "Lambda.TooManyRequestsException",
                  "Lambda.Unknown"
                ],
                "IntervalSeconds": 2,
                "MaxAttempts": 6,
                "BackoffRate": 2
              }
            ],
            "End": true
          }
        }
      },
      "Next": "Lambda DeleteXml",
      "ResultPath": null,
      "Retry": [
        {
          "ErrorEquals": [
            "States.ALL"
          ],
          "BackoffRate": 2,
          "IntervalSeconds": 1,
          "MaxAttempts": 3,
          "Comment": "Retry on failure"
        }
      ],
      "Catch": [
        {
          "ErrorEquals": [
            "States.ALL"
          ],
          "Comment": "Send message on error",
          "Next": "Notify failure to ingest"
        }
      ],
      "Label": "Map",
      "MaxConcurrency": 1000,
      "ItemsPath": "$.deathRegistrations"
    },
    "Notify failure to ingest": {
      "Type": "Task",
      "Resource": "arn:aws:states:::sns:publish",
      "Parameters": {
        "Message.$": "$",
        "TopicArn": "${module.sns.topic_arn}"
      },
      "End": true
    },
    "Lambda DeleteXml": {
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke",
      "OutputPath": "$.Payload",
      "Parameters": {
        "Payload.$": "$",
        "FunctionName": "${aws_lambda_function.delete_xml_lambda.arn}:$LATEST"
      },
      "Retry": [
        {
          "ErrorEquals": [
            "Lambda.ServiceException",
            "Lambda.AWSLambdaException",
            "Lambda.SdkClientException",
            "Lambda.TooManyRequestsException",
            "Lambda.Unknown"
          ],
          "IntervalSeconds": 2,
          "MaxAttempts": 6,
          "BackoffRate": 2
        }
      ],
      "End": true
    }
  }
}
JSON
}


data "aws_iam_policy_document" "stepfunction_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["states.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "stepfunction" {
  name               = "${var.environment}-gro-ingestion-stepfunction-ingest-file"
  assume_role_policy = data.aws_iam_policy_document.stepfunction_assume_policy.json
}

data "aws_iam_policy_document" "invoke_lambdas" {
  statement {
    effect  = "Allow"
    actions = ["lambda:InvokeFunction"]
    resources = [
      "${aws_lambda_function.split_xml_lambda.arn}:$LATEST",
      "${aws_lambda_function.map_xml_lambda.arn}:$LATEST",
      "${aws_lambda_function.delete_xml_lambda.arn}:$LATEST"
    ]
  }
}

resource "aws_iam_policy" "invoke_lambdas" {
  name   = "${var.environment}-gro-ingestion-stepfunction-ingest-file"
  policy = data.aws_iam_policy_document.invoke_lambdas.json
}

resource "aws_iam_role_policy_attachment" "stepfunction_invoke_consumer_lambdas" {
  policy_arn = aws_iam_policy.invoke_lambdas.arn
  role       = aws_iam_role.stepfunction.name
}

data "aws_iam_policy_document" "stepfunction_start_execution" {
  statement {
    effect    = "Allow"
    actions   = ["states:StartExecution"]
    resources = [aws_sfn_state_machine.stepfunction.arn]
  }
}

resource "aws_iam_policy" "stepfunction_start_execution" {
  name   = "${var.environment}-gro-ingestion-step-function-start-execution"
  policy = data.aws_iam_policy_document.stepfunction_start_execution.json
}

resource "aws_iam_role_policy_attachment" "allow_stepfunction_start_execution" {
  policy_arn = aws_iam_policy.stepfunction_start_execution.arn
  role       = aws_iam_role.stepfunction.name
}

resource "aws_iam_role_policy_attachment" "stepfunction_xray_access" {
  policy_arn = data.aws_iam_policy.xray_access.arn
  role       = aws_iam_role.stepfunction.name
}

resource "aws_s3_bucket_notification" "trigger_stepfunction" {
  bucket      = module.gro_bucket.id
  eventbridge = true
}

resource "aws_cloudwatch_event_rule" "trigger_stepfunction" {
  name        = "${var.environment}-trigger-gro-ingestion-step-function"
  description = "Trigger GRO ingestion stepfunction after file upload to S3 bucket"

  event_pattern = jsonencode({
    source      = ["aws.s3"]
    detail-type = ["Object Created"]
    detail = {
      bucket = {
        name = [module.gro_bucket.id]
      }
    }
  })
}

resource "aws_cloudwatch_event_target" "trigger_stepfunction" {
  rule      = aws_cloudwatch_event_rule.trigger_stepfunction.name
  target_id = "${var.environment}-gro-ingestion-step-function"
  arn       = aws_sfn_state_machine.stepfunction.arn
  role_arn  = aws_iam_role.event_bridge.arn
}

data "aws_iam_policy_document" "event_bridge_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      identifiers = ["events.amazonaws.com"]
      type        = "Service"
    }
  }
}

resource "aws_iam_role" "event_bridge" {
  name               = "${var.environment}-event-bridge-gro-ingestion-step-function"
  assume_role_policy = data.aws_iam_policy_document.event_bridge_assume_role.json
}

resource "aws_iam_role_policy_attachment" "allow_eventbridge_start_execution" {
  policy_arn = aws_iam_policy.stepfunction_start_execution.arn
  role       = aws_iam_role.event_bridge.name
}
