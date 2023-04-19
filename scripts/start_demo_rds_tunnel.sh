eval $(assume-role gdx)

EC2_INSTANCE=$(aws ec2 describe-instances --region eu-west-2 --filters "Name=tag:Name,Values=demo-rds-bastion-host" "Name=instance-state-name,Values=running" | jq -r '.Reservations[].Instances[].InstanceId')

aws ssm start-session \
    --region eu-west-2 \
    --target $EC2_INSTANCE \
    --document-name AWS-StartPortForwardingSessionToRemoteHost \
    --parameters host="demo-rds-db.cluster-chfyfnfoqhf6.eu-west-2.rds.amazonaws.com",portNumber="45678",localPortNumber="5434"
