resource "aws_security_group" "lb" {
  name        = "${var.environment}-lb"
  description = "Allow access to GDX data share POC LB from Cloudfront"
  vpc_id      = module.vpc.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "lb_nlb" {
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 80
  to_port                  = 80
  description              = "LB ingress rule for NLB"
  security_group_id        = aws_security_group.lb.id
  source_security_group_id = aws_security_group.nlb.id
}

#tfsec:ignore:aws-ec2-no-public-ingress-sgr
resource "aws_security_group_rule" "lb_test" {
  type              = "ingress"
  protocol          = "tcp"
  from_port         = 8080
  to_port           = 8080
  description       = "LB ingress rule for tests"
  security_group_id = aws_security_group.lb.id
  cidr_blocks       = [for ip in module.vpc.nat_gateway_public_eips : "${ip}/32"] ##asodjkhf;alksdjhf;laksjdfl;akjsdfl TODO QQ
}

resource "aws_security_group_rule" "lb_egress" {
  type              = "egress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  description       = "LB egress rule to VPC"
  security_group_id = aws_security_group.lb.id
  cidr_blocks       = [var.vpc_cidr]
}

resource "aws_security_group_rule" "lb_test_egress" {
  type              = "egress"
  from_port         = 8080
  to_port           = 8080
  protocol          = "tcp"
  description       = "LB test egress rule to VPC"
  security_group_id = aws_security_group.lb.id
  cidr_blocks       = [var.vpc_cidr]
}

resource "aws_security_group" "nlb" {
  name        = "${var.environment}-nlb"
  description = "Allow access to GDX data share POC NLB from Cloudfront"
  vpc_id      = module.vpc.vpc_id

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

resource "aws_security_group_rule" "nlb_cloudfront" {
  type              = "ingress"
  protocol          = "tcp"
  from_port         = local.is_dev ? 80 : 443
  to_port           = local.is_dev ? 80 : 443
  description       = "NLB ingress rule for cloudfront"
  security_group_id = aws_security_group.nlb.id
  prefix_list_ids   = [data.aws_ec2_managed_prefix_list.cloudfront.id]
}

resource "aws_security_group_rule" "nlb_egress" {
  type                     = "egress"
  from_port                = 80
  to_port                  = 80
  protocol                 = "tcp"
  description              = "NLB egress rule to LB"
  security_group_id        = aws_security_group.nlb.id
  source_security_group_id = aws_security_group.lb.id
}
