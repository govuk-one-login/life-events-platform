# Auto-updating security group for Cloudfront
# See https://aws.amazon.com/blogs/security/how-to-automatically-update-your-security-groups-for-amazon-cloudfront-and-aws-waf-by-using-aws-lambda/
locals {
  lb_sg_protocols = ["http", "tcp"]
  lb_sg_names     = ["cloudfront_g", "cloudfront_r"]

  lb_sg_opts = setproduct(local.lb_sg_names, local.lb_sg_protocols)

  # Central AWS IP address changes topic
  # See https://aws.amazon.com/blogs/aws/subscribe-to-aws-public-ip-address-changes-via-amazon-sns/
  update_ips_sns_arn = "arn:aws:sns:us-east-1:806199016981:AmazonIpSpaceChanged"
}

resource "aws_security_group" "lb_auto" {
  count       = length(local.lb_sg_opts)
  name_prefix = "${var.environment}-ecs-alb-auto-${count.index}-"
  description = "${local.lb_sg_opts[count.index][1]} access to GDX data share POC LB from Cloudfront for ${local.lb_sg_opts[count.index][0]}"
  vpc_id      = module.vpc.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  lifecycle {
    # Ingress will be set automatically via a Lambda
    ignore_changes        = [ingress]
    create_before_destroy = true
  }

  tags = {
    Name       = local.lb_sg_opts[count.index][0],
    AutoUpdate = "true",
    Protocol   = local.lb_sg_opts[count.index][1]
  }
}

resource "aws_iam_role" "lb_sg_update" {
  name = "${var.environment}-lb-sg-update"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy" "lb_sg_update" {
  name = "${var.environment}-lb-sg-update"
  role = aws_iam_role.lb_sg_update.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:PutLogEvents",
          "logs:CreateLogStream",
          "logs:CreateLogGroup"
        ]
        Resource = "arn:aws:logs:*:*:*"
      },
      {
        Effect = "Allow"
        Action = [
          "ec2:DescribeSecurityGroups",
        ],
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ec2:RevokeSecurityGroupIngress",
          "ec2:AuthorizeSecurityGroupIngress"
        ],
        Resource = aws_security_group.lb_auto[*].arn
      }
    ]
  })
}

resource "aws_cloudwatch_log_group" "example" {
  name              = "/aws/lambda/${aws_lambda_function.lb_sg_update.function_name}"
  retention_in_days = var.cloudwatch_retention_period
}

data "archive_file" "lb_sg_update_lambda" {
  type        = "zip"
  source_file = "${path.module}/lambdas/update_security_groups.py"
  output_path = "${path.module}/lambdas/update_security_groups.zip"
}

resource "aws_lambda_function" "lb_sg_update" {
  function_name    = "${var.environment}-lb-sg-update"
  handler          = "update_security_groups.lambda_handler"
  role             = aws_iam_role.lb_sg_update.arn
  runtime          = "python3.7"
  filename         = data.archive_file.lb_sg_update_lambda.output_path
  source_code_hash = data.archive_file.lb_sg_update_lambda.output_base64sha256
  timeout          = 10

  environment {
    variables = {
      account_tag = "gdx-data-share-poc-environment-${var.environment}"
      region      = "eu-west-2"
    }
  }
}

resource "aws_sns_topic_subscription" "lb_sg_update" {
  # Note: Subscriptions have to made in the same region as the SNS topic
  provider = aws.us-east-1

  topic_arn = local.update_ips_sns_arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.lb_sg_update.arn
}

resource "aws_lambda_permission" "lb_sg_update_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lb_sg_update.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = local.update_ips_sns_arn
}
