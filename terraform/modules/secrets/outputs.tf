output "secret_arn" {
  description = "The ARN of the Razorpay Secrets Manager secret"
  value       = aws_secretsmanager_secret.razorpay_keys.arn
}

output "secret_name" {
  description = "The name of the Razorpay Secrets Manager secret"
  value       = aws_secretsmanager_secret.razorpay_keys.name
}