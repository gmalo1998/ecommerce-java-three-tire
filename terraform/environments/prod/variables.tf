variable "aws_region" {
  default = "ap-south-1"
}

variable "project_name" {
  default = "ecomm-three-tier"
}

variable "cluster_name" {
  default = "ecomm-prod-eks"
}

variable "db_password" {
  description = "RDS MySQL Master Password"
  type        = string
  sensitive   = true
}
variable "github_repo_url" {
  description = "GitHub Repository URL"
  default     = "https://github.com/gmalo1998/ecommerce-java-three-tire"
}

variable "github_runner_token" {
  description = "GitHub Actions Self-Hosted Runner Registration Token"
  type        = string
  sensitive   = true
}
