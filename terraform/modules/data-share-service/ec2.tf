data "aws_ami" "amazon_linux" {
  most_recent = true
  filter {
    name   = "name"
    values = ["amzn2-ami-kernel-*-x86_64-gp2"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  owners = ["amazon"]
}

resource "aws_instance" "rds_bastion_host" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3a.nano"
  subnet_id                   = module.vpc.private_subnet_ids[0]
  associate_public_ip_address = false
  vpc_security_group_ids      = [aws_security_group.rds_bastion_host_sg.id]
  iam_instance_profile        = aws_iam_instance_profile.rds_bastion_instance_profile.name
  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }
  root_block_device {
    encrypted = true
  }
  tags = {
    Name = "${var.environment}-rds-bastion-host"
  }
}

resource "aws_security_group" "rds_bastion_host_sg" {
  name_prefix = "${var.environment}-rds-bastion-"
  description = "For bastion host access to GDX Data Share PoC Service RDS instances"
  vpc_id      = module.vpc.vpc_id

  egress {
    protocol    = "tcp"
    from_port   = 5432
    to_port     = 5432
    cidr_blocks = [var.vpc_cidr]
    description = "Allow egress for RDS bastion"
  }

  egress {
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = [module.vpc.private_cidr_blocks[0]]
    description = "Allow egress inside subnet to hit SSM privatelink endpoint"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_role" "rds_bastion_access_role" {
  name = "${var.environment}-ec2-rds-access-role"

  assume_role_policy = data.aws_iam_policy_document.rds_bastion_access_policy.json
}

data "aws_iam_policy" "rds_bastion_access_ssm_policy" {
  name = "AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy" "rds_bastion_access_ec2_policy" {
  name = "EC2InstanceConnect"
}

resource "aws_iam_role_policy_attachment" "rds_bastion_access_ssm" {
  role       = aws_iam_role.rds_bastion_access_role.name
  policy_arn = data.aws_iam_policy.rds_bastion_access_ssm_policy.arn
}

resource "aws_iam_role_policy_attachment" "rds_bastion_access_ec2" {
  role       = aws_iam_role.rds_bastion_access_role.name
  policy_arn = data.aws_iam_policy.rds_bastion_access_ec2_policy.arn
}

data "aws_iam_policy_document" "rds_bastion_access_policy" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }

    actions = [
      "sts:AssumeRole",
    ]
  }
}

resource "aws_iam_instance_profile" "rds_bastion_instance_profile" {
  name = "${var.environment}-ec2-rds"
  role = aws_iam_role.rds_bastion_access_role.name
}

resource "aws_security_group" "rds_bastion_host_vpc_endpoint_sg" {
  name_prefix = "${var.environment}-rds-bastion-vpc-endpoint-"
  description = "For access from the subnet which the bastion host for GDX Data Share PoC Service RDS instances lies in"
  vpc_id      = module.vpc.vpc_id

  ingress {
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = [var.vpc_cidr]
    description = "To access Systems Manager endpoints from bastion host"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_vpc_endpoint" "rds_bastion_vpc_endpoint_ssm" {
  vpc_id              = module.vpc.vpc_id
  service_name        = "com.amazonaws.eu-west-2.ssm"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc.private_subnet_ids[0]]
  private_dns_enabled = true
  security_group_ids  = [aws_security_group.rds_bastion_host_vpc_endpoint_sg.id]
}

resource "aws_vpc_endpoint" "rds_bastion_vpc_endpoint_ssm_messages" {
  vpc_id              = module.vpc.vpc_id
  service_name        = "com.amazonaws.eu-west-2.ssmmessages"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc.private_subnet_ids[0]]
  private_dns_enabled = true
  security_group_ids  = [aws_security_group.rds_bastion_host_vpc_endpoint_sg.id]
}

resource "aws_vpc_endpoint" "rds_bastion_vpc_endpoint_ec2_messages" {
  vpc_id              = module.vpc.vpc_id
  service_name        = "com.amazonaws.eu-west-2.ec2messages"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc.private_subnet_ids[0]]
  private_dns_enabled = true
  security_group_ids  = [aws_security_group.rds_bastion_host_vpc_endpoint_sg.id]
}
