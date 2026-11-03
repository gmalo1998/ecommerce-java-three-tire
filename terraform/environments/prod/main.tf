module "vpc" {
  source       = "../../modules/vpc"
  project_name = var.project_name
  aws_region   = var.aws_region
  cluster_name = var.cluster_name
}

module "ecr" {
  source       = "../../modules/ecr"
  project_name = var.project_name
}


module "rds" {
  source                     = "../../modules/rds"
  project_name               = var.project_name
  vpc_id                     = module.vpc.vpc_id
  vpc_cidr_block             = module.vpc.vpc_cidr_block
  database_subnet_group_name = module.vpc.database_subnet_group_name
  eks_node_security_group_id = module.eks.node_security_group_id
  db_password                = var.db_password
}

module "ec2_github_runner" {
  source              = "../../modules/ec2-github-runner"
  project_name        = var.project_name
  vpc_id              = module.vpc.vpc_id
  public_subnet_id    = module.vpc.public_subnets[0]
  github_repo_url     = var.github_repo_url
  github_runner_token = var.github_runner_token
}

module "eks" {
  source              = "../../modules/eks"
  project_name        = var.project_name
  cluster_name        = var.cluster_name
  vpc_id              = module.vpc.vpc_id
  private_subnets     = module.vpc.private_subnets
  runner_iam_role_arn = module.ec2_github_runner.runner_iam_role_arn
}

output "vpc_id" { value = module.vpc.vpc_id }
output "ecr_repos" { value = module.ecr.repository_urls }
output "rds_endpoint" { value = module.rds.rds_endpoint }
output "eks_cluster_name" { value = module.eks.cluster_name }
output "runner_public_ip" { value = module.ec2_github_runner.runner_public_ip }



