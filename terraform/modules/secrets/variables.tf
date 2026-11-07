variable "razorpay_api_key" {
  type        = string
  description = "Razorpay API Key"
  sensitive   = true
}

variable "razorpay_api_secret" {
  type        = string
  description = "Razorpay API Secret"
  sensitive   = true
}

variable "db_url" {
  type        = string
  description = "Database JDBC URL"
}

variable "db_username" {
  type        = string
  description = "Database Username"
}

variable "db_password" {
  type        = string
  description = "Database Password"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "JWT Secret Key"
  sensitive   = true
}

variable "mail_host" {
  type        = string
  description = "Spring Mail Host"
  default     = "smtp.gmail.com"
}

variable "mail_port" {
  type        = string
  description = "Spring Mail Port"
  default     = "587"
}

variable "mail_username" {
  type        = string
  description = "Spring Mail Username"
}

variable "mail_password" {
  type        = string
  description = "Spring Mail App Password"
  sensitive   = true
}

variable "stripe_api_key" {
  type        = string
  description = "Stripe API Key"
  sensitive   = true
}