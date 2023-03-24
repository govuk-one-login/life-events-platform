if [ $# -eq 1 ];
then
  export AWS_PROFILE=$1
fi;

EC2_INSTANCE=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=demo-rds-bastion-host" "Name=instance-state-name,Values=running" | jq -r '.Reservations[].Instances[].InstanceId')

aws ssm start-session \
    --target $EC2_INSTANCE \
    --document-name AWS-StartPortForwardingSessionToRemoteHost \
    --parameters host="demo-rds-db.cluster-chfyfnfoqhf6.eu-west-2.rds.amazonaws.com",portNumber="5432",localPortNumber="5434"
