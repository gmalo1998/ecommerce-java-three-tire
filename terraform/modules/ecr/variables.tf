variable "project_name" {}
variable "repository_names" {
  type    = list(string)
  default = ["frontend", "backend", "ai-service"]
}

