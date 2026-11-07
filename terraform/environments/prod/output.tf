output "vpc_id" { value = module.vpc.vpc_id }
output "ecr_repos" { value = module.ecr.repository_urls }
output "rds_endpoint" { value = module.rds.rds_endpoint }
output "eks_cluster_name" { value = module.eks.cluster_name }
output "runner_public_ip" { value = module.ec2_github_runner.runner_public_ip }