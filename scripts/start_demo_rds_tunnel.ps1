assume-role.exe gdx | Invoke-Expression

$EC2_INSTANCE = (aws ec2 describe-instances --region eu-west-2 --filters "Name=tag:Name,Values=demo-rds-bastion-host" "Name=instance-state-name,Values=running" | ConvertFrom-Json).Reservations[0].Instances[0].InstanceId

aws ssm start-session `
    --region eu-west-2 `
    --target $EC2_INSTANCE `
    --document-name AWS-StartPortForwardingSessionToRemoteHost `
    --parameters "host=demo-rds-db.cluster-chfyfnfoqhf6.eu-west-2.rds.amazonaws.com,portNumber=5432,localPortNumber=5434"
