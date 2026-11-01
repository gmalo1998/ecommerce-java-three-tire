/*
Terraform Data Source
A Data Source retrieves information from an external system, such as AWS, through the AWS API.
The aws_caller_identity data source automatically retrieves three attributes:
account_id – AWS Account ID.
arn – ARN of the currently authenticated IAM identity.
user_id – Unique identifier of the IAM identity.
Difference Between Variable and Data Source
Variable: Used to accept input values from external sources, such as users or .tfvars files.
Data Source: Used to retrieve information about existing resources or external systems.
*/
data "aws_caller_identity" "current" {} 