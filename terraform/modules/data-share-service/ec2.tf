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
  instance_type               = "t3a.micro"
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

  ingress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    self        = true
    description = "Allow ingress to members of security group"
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = [var.vpc_cidr]
    description = "Allow egress for RDS bastion"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_role" "rds_bastion_access_role" {
  name = "${var.environment}-ec2-rds-access-role"

  assume_role_policy = data.aws_iam_policy_document.rds_bastion_access_policy.json
}

data "aws_iam_policy" "rds_bastion_access_policy" {
  name = "AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "rds_bastion_access" {
  role       = aws_iam_role.rds_bastion_access_role.name
  policy_arn = data.aws_iam_policy.rds_bastion_access_policy.arn
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
  name = "ec2_rds"
  role = aws_iam_role.rds_bastion_access_role.name
}

resource "aws_eip" "rds_bastion_ip" {
  instance = aws_instance.rds_bastion_host.id
}
