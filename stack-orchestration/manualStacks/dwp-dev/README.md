# Manual stacks - Account Setup

## Intro

The CloudFormation template sets up all the manual 1 time setup resources for DWP development access.

This Stack is deployed manually in integration.

## Deployment

To deploy the template to the appropriate AWS account, ensure you are at the root of the project.

### Creating a New Stack

```bash
aws cloudformation create-stack --stack-name dwp-dev \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="integration" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="integration" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```

### Updating the Stack

```bash
aws cloudformation update-stack --stack-name dwp-dev \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="integration" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="integration" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```
