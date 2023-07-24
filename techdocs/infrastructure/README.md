# Deploy domain stack
In the dev account:
```shell
aws cloudformation create-stack --stack-name techdocs-domain \
    --template-body file://$(pwd)/domain.yaml \
    --tags Key=Product,Value="GOV.UK Sign In" \
           Key=System,Value="Life Events Platform" \
           Key=Environment,Value="dev" \
           Key=Owner,Value="di-life-events-platform@digital.cabinet-office.gov.uk"
```
