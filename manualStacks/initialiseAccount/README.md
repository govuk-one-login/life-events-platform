# Manual stacks - DNS Hosted Zones

Edited
from [here](https://github.com/alphagov/di-accounts-infra/blob/9ddff8f2f9683a518a6a844c3918bfa67cae53e1/platform-dns/README.md).

## Intro

## Pre Deployment

Before deployment, make sure that granting access for AWS Chatbot to slack occurs
from [here](https://govukverify.atlassian.net/wiki/spaces/PLAT/pages/3377168419/Slack+build+notifications+-+via+AWS+Chatbot).

## Deployment

To deploy the template to the appropriate AWS account, ensure you are at the root of the project.

Replace `<environment>` with `dev`, `build`, `staging`, `integration`, `production`, and `<channel-id>` with the desired
Slack Channel ID for build notifications in either of the commands below.

### Creating a New Stack

The name of the stack is `ia`, this is due to limitations in the allowed length of the nested stacks when calling them
from secure pipelines.

Set your AWS profile to the correct environment, the run:

```bash
aws cloudformation create-stack --stack-name ia \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="<environment>" \
  --parameters ParameterKey=SlackChannelId,ParameterValue="<channel-id>" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="<environment>" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```

### Updating the Stack

Set your AWS profile to the correct environment, the run:

```bash
aws cloudformation update-stack --stack-name ia \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="<environment>" \
  --parameters ParameterKey=SlackChannelId,ParameterValue="<channel-id>" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="<environment>" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```
