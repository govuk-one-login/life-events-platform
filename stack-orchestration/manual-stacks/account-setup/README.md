# Manual stacks - Account Setup

## Intro

The CloudFormation template sets up all the manual 1 time setup resources for our accounts.

This Stack is deployed manually once per account/environment.

## Deployment

To deploy the template to the appropriate AWS account, ensure you are at the root of the project.

Replace `<environment>` with `dev`, `build`, `staging`, `integration`, `production` in either of the commands below.

### Creating a New Stack

Set your AWS profile to the correct environment, then run `export ENVIRONMENT=<environment>`, then run:

```bash
aws cloudformation create-stack --stack-name account-setup \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="$ENVIRONMENT" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="$ENVIRONMENT" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```

### Updating the Stack

Set your AWS profile to the correct environment, run `export ENVIRONMENT=<environment>` then run:

```bash
aws cloudformation update-stack --stack-name account-setup \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="$ENVIRONMENT" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="$ENVIRONMENT" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```
