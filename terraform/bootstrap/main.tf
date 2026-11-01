# S3 bucket for the terraform state file managementS

resource "aws_s3_bucket" "s3_bucket_state_backend" {
    bucket = "${var.project_name}-tfstate-${data.aws_caller_identity.current}"
    force_destroy = false  
    /*If the S3 bucket is not empty and we try to delete it, Terraform will throw an error because force_destroy = false 
    does not allow automatic deletion of objects inside the bucket.*/

    tags = {
        Name = "Terraform State Storage"
        Environmet = "prod"
        ManagedBy   = "Terraform"
        Project     = var.project_name
    }
}

/*Tags are basically metadata used to identify AWS resources and provide additional information about them.
They help us identify resources based on project, environment, ownership, and cost management. */ 

# 1. S3 Bucket for Terraform State
resource "aws_s3_bucket" "terraform_state" {
  bucket        = "${var.project_name}-tfstate-${data.aws_caller_identity.current.account_id}"
  force_destroy = false

  tags = {
    Name        = "Terraform State Bucket"
    Environment = "prod"
  }
}

# Enable Versioning so we can recover state if needed
resource "aws_s3_bucket_versioning" "state_versioning" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Enable Server-Side Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "state_encryption" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
# it will encrypt the bucket so the authorized access ca read this

# Block Public Access
resource "aws_s3_bucket_public_access_block" "state_public_access" {
  bucket                  = aws_s3_bucket.terraform_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
# people will not be able to access publically  

# DynamDB resource creation

resource "aws_dynamodb_table" "terraform_locks" {
  name           = "${var.project_name}-tf-locks"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "LockID"
  

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name        = "dynamodb-table-state-locking"
    Environment = "prod"
  }
}