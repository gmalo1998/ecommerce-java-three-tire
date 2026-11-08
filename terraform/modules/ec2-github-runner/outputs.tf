output "runner_public_ip" { value = aws_instance.github_runner.public_ip }
output "runner_iam_role_arn" { value = aws_iam_role.runner_role.arn }