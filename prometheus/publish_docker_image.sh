ACCOUNT=$(aws sts get-caller-identity | jq .Account --raw-output)
ECR_URL=$ACCOUNT.dkr.ecr.eu-west-2.amazonaws.com

docker build -f Adot.Dockerfile -t prometheus-adot:latest .
TAG=$(docker inspect prometheus-adot:latest | jq ".[0].Id" --raw-output | tr -d "\n" | tail -c 64)

aws ecr get-login-password --region eu-west-2 \
  | docker login --username AWS --password-stdin "$ECR_URL"
docker tag prometheus-adot "$ECR_URL/prometheus-adot:$TAG"
docker push "$ECR_URL/prometheus-adot:$TAG"
