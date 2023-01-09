data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"] # Canonical
}

resource "aws_network_interface" "rds_bastion_eni" {
  subnet_id       = module.vpc.public_subnet_ids[0]
  security_groups = [aws_security_group.rds_bastion_host_sg.id]
  attachment {
    device_index = 0
    instance     = aws_instance.rds_bastion_host.id
  }
}

resource "aws_instance" "rds_bastion_host" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t4g.nano"
  associate_public_ip_address = true
  key_name                    = aws_key_pair.rds_bastion_key_pair.key_name
  metadata_options {
    http_endpoint = "disabled"
  }
  root_block_device {
    encrypted = true
  }
}

resource "aws_key_pair" "rds_bastion_key_pair" {
  public_key = module.tls_private_key.public_key_pem
  key_name   = "rds-bastion-key-pair-${var.environment}"
}

#tfsec:ignore:aws-ec2-no-public-ingress-sgr
#tfsec:ignore:aws-ec2-no-public-egress-sgr
resource "aws_security_group" "rds_bastion_host_sg" {
  name_prefix = "${var.environment}-rds-bastion-"
  description = "For bastion host access to GDX Data Share PoC Service RDS instances"
  vpc_id      = module.vpc.vpc_id

  ingress {
    protocol    = "tcp"
    from_port   = 22
    to_port     = 22
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow SSH for RDS bastion"
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow egress for RDS bastion"
  }

  lifecycle {
    create_before_destroy = true
  }
}

module "tls_private_key" {
  name   = "rds-bastion-key-${var.environment}"
  source = "github.com/hashicorp-modules/tls-private-key?ref=5918adb7efe39d7b36f0185569684b1b73f18126"
}
