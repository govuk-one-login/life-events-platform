eval $(assume-role gdx)

EC2_INSTANCE=$(aws ec2 describe-instances --region eu-west-2 --filters "Name=tag:Name,Values=dev-rds-bastion-host" "Name=instance-state-name,Values=running" | jq '.Reservations[].Instances[].InstanceId')

aws ssm start-session \
    --region eu-west-2 \
    --target $EC2_INSTANCE \
    --document-name AWS-StartPortForwardingSessionToRemoteHost \
    --parameters host="dev-rds-db.cluster-chfyfnfoqhf6.eu-west-2.rds.amazonaws.com",portNumber="5432",localPortNumber="5433"
