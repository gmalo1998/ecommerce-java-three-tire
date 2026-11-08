variable "eks_node_security_group_id" {}
variable "project_name" {}
variable "vpc_id" {}
variable "vpc_cidr_block" {}
variable "database_subnet_group_name" {}
variable "db_name" { default = "ecommerce_db" }
variable "db_username" { default = "admin" }
variable "db_password" { sensitive = true }

