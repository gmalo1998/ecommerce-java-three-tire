# terraform/modules/eks/main.tf
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.31"

  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  # Gives the Terraform IAM identity cluster-admin access automatically
  enable_cluster_creator_admin_permissions = true

  vpc_id                   = var.vpc_id
  subnet_ids               = var.private_subnets
  control_plane_subnet_ids = var.private_subnets

  # Enable OIDC Provider for IRSA (Needed for AWS Load Balancer Controller & EBS CSI)
  enable_irsa = true

  cluster_addons = {
    coredns                = { most_recent = true }
    kube-proxy             = { most_recent = true }
    vpc-cni                = { most_recent = true }
    aws-ebs-csi-driver     = {
      most_recent              = true
      service_account_role_arn = module.ebs_csi_irsa_role.iam_role_arn
    }
  }

  eks_managed_node_groups = {
    # Primary Node Group for Frontend, Backend, ArgoCD, Monitoring
    app_nodes = {
      name           = "ecomm-app-ng"
      instance_types = ["t3.large"] # 2 vCPU, 8GB RAM per node (Prod minimum for full stack)
      min_size       = 2
      max_size       = 4
      desired_size   = 2
      disk_size      = 30

      labels = {
        role        = "application"
        environment = "prod"
      }
    }
  }

  tags = {
    Project     = var.project_name
    Environment = "prod"
  }
}

# IAM Role for EBS CSI Driver (Required for PVCs in Prometheus/Grafana/Stateful workloads)
module "ebs_csi_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.30"

  role_name             = "${var.cluster_name}-ebs-csi-role"
  attach_ebs_csi_policy = true

  oidc_providers = {
    ex = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }
}

# Grant EC2 GitHub Runner access to EKS Cluster via EKS Access Entry
resource "aws_eks_access_entry" "runner_access" {
  cluster_name  = module.eks.cluster_name
  principal_arn = var.runner_iam_role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "runner_admin" {
  cluster_name  = module.eks.cluster_name
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  principal_arn = var.runner_iam_role_arn

  access_scope {
    type = "cluster"
  }
  depends_on = [module.eks]
}
# terraform/modules/eks/main.tf er niche ei code-tuku add koro:

# IAM Role for AWS Load Balancer Controller (Required for Ingress -> AWS ALB)
module "lb_controller_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.30"

  role_name                              = "${var.cluster_name}-aws-lb-controller-role"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    ex = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }
}

# Add these additional outputs at the bottom of terraform/modules/eks/main.tf
output "cluster_certificate_authority_data" {
  value = module.eks.cluster_certificate_authority_data
}

output "node_security_group_id" {
  value = module.eks.node_security_group_id
}

output "lb_controller_role_arn" {
  value = module.lb_controller_irsa_role.iam_role_arn
}

variable "project_name" {}
variable "cluster_name" {}
variable "vpc_id" {}
variable "private_subnets" { type = list(string) }
variable "runner_iam_role_arn" {}

output "cluster_name" { value = module.eks.cluster_name }
output "cluster_endpoint" { value = module.eks.cluster_endpoint }
output "oidc_provider_arn" { value = module.eks.oidc_provider_arn }