

variable "project_name" {}
variable "cluster_name" {}
variable "vpc_id" {}
variable "private_subnets" { type = list(string) }
variable "runner_iam_role_arn" {}

