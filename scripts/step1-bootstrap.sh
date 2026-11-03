#!/bin/bash
# scripts/step1-bootstrap.sh
set -e

echo "Starting Terraform State Bootstrap..."
cd terraform/bootstrap
terraform init
terraform apply -auto-approve

BUCKET_NAME=$(terraform output -raw s3_bucket_name)
DYNAMO_TABLE=$(terraform output -raw dynamodb_table_name)

echo "Bootstrap Complete!"
echo "S3 Bucket: $BUCKET_NAME"
echo "DynamoDB Lock Table: $DYNAMO_TABLE"

echo "Initializing Production Environment with Remote State & Locking..."
cd ../environments/prod
terraform init -backend-config="bucket=$BUCKET_NAME"

echo "Step 1 Initialization Ready! Run 'terraform plan' or move to Step 2 to add EKS & Runner."