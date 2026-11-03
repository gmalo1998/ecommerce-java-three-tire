# terraform/modules/ec2-github-runner/main.tf
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

resource "aws_security_group" "runner_sg" {
  name        = "${var.project_name}-runner-sg"
  description = "Security group for GitHub Action Self-Hosted Runner"
  vpc_id      = var.vpc_id

  ingress {
    description = "SSH Access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Production-e nijer IP deowa bhalo
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# IAM Role for EC2 Runner (Allows ECR Push & EKS Describe/Admin)
resource "aws_iam_role" "runner_role" {
  name = "${var.project_name}-github-runner-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecr_power_user" {
  role       = aws_iam_role.runner_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

resource "aws_iam_role_policy_attachment" "ssm_managed" {
  role       = aws_iam_role.runner_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "runner_profile" {
  name = "${var.project_name}-runner-profile"
  role = aws_iam_role.runner_role.name
}

# EC2 Instance for GitHub Runner
resource "aws_instance" "github_runner" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t3.medium" # 2 vCPU, 4GB RAM for smooth Maven & Docker builds
  subnet_id                   = var.public_subnet_id
  vpc_security_group_ids      = [aws_security_group.runner_sg.id]
  iam_instance_profile        = aws_iam_instance_profile.runner_profile.name
  associate_public_ip_address = true

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = templatefile("${path.module}/../../../scripts/runner-setup.sh", {
    github_repo_url     = var.github_repo_url
    github_runner_token = var.github_runner_token
  })

  tags = {
    Name        = "${var.project_name}-github-runner"
    Environment = "prod"
  }
}

variable "project_name" {}
variable "vpc_id" {}
variable "public_subnet_id" {}
variable "github_repo_url" {}
variable "github_runner_token" { sensitive = true }

output "runner_public_ip" { value = aws_instance.github_runner.public_ip }
output "runner_iam_role_arn" { value = aws_iam_role.runner_role.arn }