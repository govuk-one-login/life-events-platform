resource "aws_security_group" "lb_cloudfront" {
  name        = "${var.environment}-lb-cloudfront"
  description = "Allow access to GDX data share POC LB from Cloudfront"
  vpc_id      = module.vpc.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

resource "aws_security_group_rule" "lb_cloudfront" {
  type              = "ingress"
  protocol          = "tcp"
  from_port         = 80
  to_port           = 80
  description       = "LB ingress rule for cloudfront"
  security_group_id = aws_security_group.lb_cloudfront.id
  prefix_list_ids   = [data.aws_ec2_managed_prefix_list.cloudfront.id]
}

#tfsec:ignore:aws-ec2-no-public-ingress-sgr
resource "aws_security_group_rule" "lb_test" {
  type              = "ingress"
  protocol          = "tcp"
  from_port         = 8080
  to_port           = 8080
  description       = "LB ingress rule for tests"
  security_group_id = aws_security_group.lb_cloudfront.id
  cidr_blocks       = [for ip in module.vpc.nat_gateway_public_eips : "${ip}/32"]
}

resource "aws_security_group_rule" "lb_egress" {
  type              = "egress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  description       = "LB egress rule to VPC"
  security_group_id = aws_security_group.lb_cloudfront.id
  cidr_blocks       = [var.vpc_cidr]
}

resource "aws_security_group_rule" "lb_test_egress" {
  type              = "egress"
  from_port         = 8080
  to_port           = 8080
  protocol          = "tcp"
  description       = "LB test egress rule to VPC"
  security_group_id = aws_security_group.lb_cloudfront.id
  cidr_blocks       = [var.vpc_cidr]
}

data "aws_iam_policy_document" "lb_sg_update_assume_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}
