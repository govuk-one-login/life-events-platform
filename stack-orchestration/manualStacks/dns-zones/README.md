# Manual stacks - DNS Hosted Zones

Edited
from [here](https://github.com/alphagov/di-accounts-infra/blob/9ddff8f2f9683a518a6a844c3918bfa67cae53e1/platform-dns/README.md)

## Intro

The CloudFormation template creates a hosted zone for `<subdomain>.account.gov.uk`
or `<subdomain>.<environment>.account.gov.uk` if environment is not `production`.

This Stack is deployed manually once per account/environment
as part of the DNS set up process.

Once the hosted zone(s) is created, there will be a nameserver record created for each zone.

Once deployed, the nameserver record lists the name servers that need to be added to appropriate environment's
cloudformation file in
the [di-domains](https://github.com/alphagov/di-domains/blob/main/cloudformation/domain/template.yaml) repo.

N.B. the hosted zone(s) created by this template are retained even when the Stack is deleted.

### Domains

The template creates a Hosted Zone for the following subdomain(s):

- `life-events`

## Deployment

To deploy the template to the appropriate AWS account, ensure you are at the root of the project.

Replace `<environment>` with `dev`, `build`, `staging`, `integration`, `production` in either of the commands below.

### Creating a New Stack

Set your AWS profile to the correct environment, then run `export ENVIRONMENT=<environment>`, then run:

```bash
aws cloudformation create-stack --stack-name dns-zones \
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
aws cloudformation update-stack --stack-name dns-zones \
  --template-body file://$(pwd)/template.yaml \
  --region eu-west-2 \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameters ParameterKey=Environment,ParameterValue="$ENVIRONMENT" \
  --tags Key=Product,Value="GOV.UK Sign In" \
         Key=System,Value="Life Events Platform" \
         Key=Environment,Value="$ENVIRONMENT" \
         Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```

### Stack Outputs

| Type         | Name                    | Description                           |
|--------------|-------------------------|---------------------------------------|
| Stack Export | `HostedZoneNameServers` | Comma separated list of Nameservers   |
| Stack Export | `HostedZoneId`          | Id of the Route 53 Hosted Zone        |
| Stack Export | `CertifcateArn`         | Arn of the Certificate for the domain |
