resource "aws_iam_role" "external_secrets" {
  name = "ecomm-prod-eks-external-secrets-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"

    Statement = [{
      Effect = "Allow"

      Principal = {
        Federated = module.eks.oidc_provider_arn
      }

      Action = "sts:AssumeRoleWithWebIdentity"

      Condition = {
        StringEquals = {
          "${replace(module.eks.cluster_oidc_issuer_url, "https://", "")}:aud" = "sts.amazonaws.com"

          "${replace(module.eks.cluster_oidc_issuer_url, "https://", "")}:sub" = "system:serviceaccount:ecommerce-apps:ecomm-sa"
        }
      }
    }]
  })

  tags = {
    Name        = "ecomm-prod-eks-external-secrets-role"
    Project     = var.project_name
    Environment = "prod"
  }
}