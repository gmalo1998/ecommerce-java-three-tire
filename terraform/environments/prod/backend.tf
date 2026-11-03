# terraform/environments/prod/backend.tf
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.30"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.13"
    }
  }

  # Note: Bootstrap script run korle bucket name automatic update ba pass kora jabe
  backend "s3" {
    bucket         = "ecomm-three-tier-tfstate-975324005004"
    key            = "prod/terraform.tfstate"
    region         = "ap-south-1"
    dynamodb_table = "ecomm-three-tier-tf-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}