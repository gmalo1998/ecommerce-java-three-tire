variable "cluster_id" {}
variable "cluster_endpoint" {}
variable "cluster_certificate_data" {}
variable "cluster_name" {}

# Helm Provider configuration for the EKS cluster
provider "helm" {
  kubernetes {
    host                   = var.cluster_endpoint
    cluster_ca_certificate = base64decode(var.cluster_certificate_data)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", var.cluster_name]
    }
  }
}

# Install Prometheus and Grafana using Helm chart (Kube Prometheus Stack)
resource "helm_release" "prometheus_stack" {
  name             = "prometheus-stack"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  namespace        = "monitoring"
  create_namespace = true
  version          = "58.2.1"
}