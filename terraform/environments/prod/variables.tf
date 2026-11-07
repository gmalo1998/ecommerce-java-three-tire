
variable "aws_region" {
  description = "AWS deployment region"
  type        = string
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
}

variable "cluster_name" {
  description = "EKS cluster name"
  type        = string
}

variable "db_password" {
  description = "RDS database password"
  type        = string
  sensitive   = true
}

variable "github_repo_url" {
  description = "GitHub repository URL for self-hosted runner"
  type        = string
}

variable "github_runner_token" {
  description = "GitHub Actions runner registration token"
  type        = string
  sensitive   = true
}
