aws cloudformation create-stack --stack-name github-identity \
  --template-url https://template-storage-templatebucket-1upzyw6v9cs42.s3.amazonaws.com/github-identity/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_IAM \
  --parameters ParameterKey=System,ParameterValue="Life Events Platform" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="build" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"

aws cloudformation create-stack --stack-name aws-signer \
  --template-url https://template-storage-templatebucket-1upzyw6v9cs42.s3.amazonaws.com/signer/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_IAM \
  --parameters ParameterKey=System,ParameterValue="Life Events Platform" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="build" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"

