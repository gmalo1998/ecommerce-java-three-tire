#!/bin/bash
# scripts/runner-setup.sh
set -e

exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "Starting DevOps CI Runner Setup..."

# 1. System Update & Essential Packages
apt-get update -y
apt-get upgrade -y
apt-get install -y curl wget unzip git jq tar apt-transport-https ca-certificates gnupg lsb-release software-properties-common

# 2. Install Docker
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu

# 3. Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip

# 4. Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
rm -f kubectl

# 5. Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# 6. Install Trivy (Container & File Security Scanner)
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | tee /usr/share/keyrings/trivy.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | tee -a /etc/apt/sources.list.d/trivy.list
apt-get update -y
apt-get install -y trivy

# 7. Install Java 17 & Maven (For Spring Boot CI Build/Test)
apt-get install -y openjdk-17-jdk maven

# 8. Install Node.js 20 & pnpm (For React TypeScript Frontend Build/Test)
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs
npm install -g pnpm

# 9. Configure and Start GitHub Actions Self-Hosted Runner
mkdir -p /home/ubuntu/actions-runner && cd /home/ubuntu/actions-runner
RUNNER_VERSION=$(curl -s https://api.github.com/repos/actions/runner/releases/latest | jq -r '.tag_name' | sed 's/v//')
curl -o actions-runner-linux-x64.tar.gz -L "https://github.com/actions/runner/releases/download/v$${RUNNER_VERSION}/actions-runner-linux-x64-$${RUNNER_VERSION}.tar.gz"
tar xzf ./actions-runner-linux-x64.tar.gz
rm -f actions-runner-linux-x64.tar.gz
chown -R ubuntu:ubuntu /home/ubuntu/actions-runner

# Register Runner (Variables injected by Terraform templatefile)
sudo -u ubuntu ./config.sh --url "${github_repo_url}" --token "${github_runner_token}" --name "aws-prod-runner" --labels "self-hosted,Linux,X64,aws-prod" --unattended --replace

# Install and start runner service
./svc.sh install ubuntu
./svc.sh start

echo "DevOps CI Runner Setup Complete!"