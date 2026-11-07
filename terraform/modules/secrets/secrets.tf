provider "aws" {
  region = "ap-south-1" # Tomar AWS region
}


# 1. Database and Application Credentials Secret
resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "ecommerce/db-credentials"
  description = "Database and application configuration credentials for the E-commerce app"
}

resource "aws_secretsmanager_secret_version" "db_credentials_value" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    SPRING_DATASOURCE_URL      = var.db_url
    SPRING_DATASOURCE_USERNAME = var.db_username
    SPRING_DATASOURCE_PASSWORD = var.db_password
    JWT_SECRET_KEY             = var.jwt_secret
    SPRING_MAIL_HOST           = var.mail_host
    SPRING_MAIL_PORT           = var.mail_port
    SPRING_MAIL_USERNAME       = var.mail_username
    SPRING_MAIL_PASSWORD       = var.mail_password
    STRIPE_API_KEY             = var.stripe_api_key
  })
}

# 2. Razorpay Payment Gateway Secret
resource "aws_secretsmanager_secret" "razorpay_keys" {
  name        = "ecommerce/razorpay-keys"
  description = "Razorpay API keys for the E-commerce Application"
}

resource "aws_secretsmanager_secret_version" "razorpay_keys_value" {
  secret_id     = aws_secretsmanager_secret.razorpay_keys.id
  secret_string = jsonencode({
    RAZORPAY_API_KEY    = var.razorpay_api_key
    RAZORPAY_API_SECRET = var.razorpay_api_secret
  })
}