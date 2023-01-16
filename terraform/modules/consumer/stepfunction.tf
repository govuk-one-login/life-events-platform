resource "aws_sfn_state_machine" "consumer" {
  definition = <<JSON
{
  "StartAt": "Lambda GetEvents",
  "States": {
    "Lambda GetEvents": {
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke",
      "OutputPath": "$.Payload",
      "Parameters": {
        "Payload.$": "$",
        "FunctionName": "arn:aws:lambda:eu-west-2:776473272850:function:dev-get-events:$LATEST"
      },
      "Retry": [
        {
          "ErrorEquals": [
            "Lambda.ServiceException",
            "Lambda.AWSLambdaException",
            "Lambda.SdkClientException",
            "Lambda.TooManyRequestsException"
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
        "StartAt": "Lambda GetEvent",
        "States": {
          "Lambda GetEvent": {
            "Type": "Task",
            "Resource": "arn:aws:states:::lambda:invoke",
            "OutputPath": "$.Payload",
            "Parameters": {
              "Payload.$": "$",
              "FunctionName": "arn:aws:lambda:eu-west-2:776473272850:function:dev-get-event:$LATEST"
            },
            "Retry": [
              {
                "ErrorEquals": [
                  "Lambda.ServiceException",
                  "Lambda.AWSLambdaException",
                  "Lambda.SdkClientException",
                  "Lambda.TooManyRequestsException"
                ],
                "IntervalSeconds": 2,
                "MaxAttempts": 6,
                "BackoffRate": 2
              }
            ],
            "Next": "Lambda DeleteEvent"
          },
          "Lambda DeleteEvent": {
            "Type": "Task",
            "Resource": "arn:aws:states:::lambda:invoke",
            "OutputPath": "$.Payload",
            "Parameters": {
              "Payload.$": "$",
              "FunctionName": "arn:aws:lambda:eu-west-2:776473272850:function:dev-delete-event:$LATEST"
            },
            "Retry": [
              {
                "ErrorEquals": [
                  "Lambda.ServiceException",
                  "Lambda.AWSLambdaException",
                  "Lambda.SdkClientException",
                  "Lambda.TooManyRequestsException"
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
      "End": true,
      "ItemsPath": "$.event_ids",
      "Parameters": {
        "event_id.$": "$$.Map.Item.Value"
      },
      "MaxConcurrency": 100,
      "Label": "Map"
    }
  }
}
JSON
  name       = "${var.environment}-consumer"
  role_arn   = aws_iam_role.stepfunction.arn
  tracing_configuration {
    enabled = true
  }
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
  name               = "${var.environment}-consumer-stepfunction"
  assume_role_policy = data.aws_iam_policy_document.stepfunction_assume_policy.json
}

data "aws_iam_policy_document" "invoke_lambdas" {
  statement {
    effect  = "Allow"
    actions = ["lambda:InvokeFunction"]
    resources = [
      "${aws_lambda_function.get_events.arn}:$LATEST",
      "${aws_lambda_function.get_event.arn}:$LATEST",
      "${aws_lambda_function.delete_event.arn}:$LATEST"
    ]
  }
}

resource "aws_iam_policy" "invoke_consumer_lambdas" {
  name   = "${var.environment}-invoke-consumer-lambdas"
  policy = data.aws_iam_policy_document.invoke_lambdas.json
}

resource "aws_iam_role_policy_attachment" "stepfunction_invoke_consumer_lambdas" {
  policy_arn = aws_iam_policy.invoke_consumer_lambdas.arn
  role       = aws_iam_role.stepfunction.name
}

data "aws_iam_policy_document" "stepfunction_start_execution" {
  statement {
    effect    = "Allow"
    actions   = ["states:StartExecution"]
    resources = [aws_sfn_state_machine.consumer.arn]
  }
}

resource "aws_iam_policy" "stepfunction_start_execution" {
  name   = "${var.environment}-consumer-stepfunction-start-execution"
  policy = data.aws_iam_policy_document.stepfunction_start_execution.json
}

resource "aws_iam_role_policy_attachment" "allow_stepfunction_start_execution" {
  policy_arn = aws_iam_policy.stepfunction_start_execution.arn
  role       = aws_iam_role.stepfunction.name
}

resource "aws_iam_role_policy_attachment" "stepfunction_xray_access" {
  role       = aws_iam_role.stepfunction.name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}

resource "aws_cloudwatch_event_rule" "schedule" {
  name                = "${var.environment}ConsumerStepfunctionSchedule"
  description         = "Schedule for stepfunction to consume events"
  schedule_expression = var.schedule
}

resource "aws_cloudwatch_event_target" "stepfunction" {
  arn       = aws_sfn_state_machine.consumer.arn
  target_id = "consumer_stepfunction"
  rule      = aws_cloudwatch_event_rule.schedule.name
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
  name               = "${var.environment}-event-bridge-schedule-consumer"
  assume_role_policy = data.aws_iam_policy_document.event_bridge_assume_role.json
}

resource "aws_iam_role_policy_attachment" "allow_eventbridge_start_execution" {
  policy_arn = aws_iam_policy.stepfunction_start_execution.arn
  role       = aws_iam_role.event_bridge.name
}
