output "s3_state_bucket" {
value = aws_s3_bucket.s3_bucket_state_backend.bucket
}

output "s3_state_bucket_region" {
    value =  var.aws_region
}
output "dynamodb_table" {
    value = aws_dynamodb_table.terraform_locks.name
}