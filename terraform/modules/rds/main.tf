# terraform/modules/rds/main.tf
resource "aws_security_group" "rds_sg" {
  name        = "${var.project_name}-rds-sg"
  description = "Allow MySQL traffic from inside VPC"
  vpc_id      = var.vpc_id

  ingress {
    description = "MySQL from VPC"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr_block]
  }
  
# terraform/modules/rds/main.tf er bhitore aws_security_group.rds_sg te ei ingress block-ti add koro:

  ingress {
    description     = "Allow MySQL from EKS Worker Nodes"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.eks_node_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "mysql" {
  identifier             = "${var.project_name}-mysql"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro" # Production-e chaile db.t3.small ba medium korte paro
  allocated_storage      = 20
  max_allocated_storage  = 50            # Storage Autoscaling enabled
  storage_type           = "gp3"
  storage_encrypted      = true

  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password

  db_subnet_group_name   = var.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = false
  skip_final_snapshot    = true # Prod test sheshe destroy korar subidharthe

  tags = {
    Name        = "${var.project_name}-mysql"
    Environment = "prod"
  }
}

variable "eks_node_security_group_id" {}
variable "project_name" {}
variable "vpc_id" {}
variable "vpc_cidr_block" {}
variable "database_subnet_group_name" {}
variable "db_name" { default = "ecommerce_db" }
variable "db_username" { default = "admin" }
variable "db_password" { sensitive = true }

output "rds_endpoint" { value = aws_db_instance.mysql.endpoint }
output "rds_address" { value = aws_db_instance.mysql.address }