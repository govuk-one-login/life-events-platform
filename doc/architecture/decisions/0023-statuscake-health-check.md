# 23. Deployment of StatusCake - terraform approach

[Next >>](0024-flat-file-ingestion-poc.md)

Date: 2023-04-17

## Status

Accepted

## Context
We want to monitor the uptime of this service with StatusCake.  This required a new API key to be stored in Param Store as a secret and this
will mean a two-step terraform build.

## Decision
The StatusCake service has a supported terraform [provider](https://registry.terraform.io/providers/StatusCakeDev/statuscake/latest/docs)
and requires an API key to function.  The integration of this monitoring service is done in the `shared` deployment package
for dev and demo environment.  This shared environment uses the parameter store in AWS to hold the API key.

The production environment will require a new API secret to be setup and stored in the parameter store of the production AWS account.

The deployment of the statuscake integration is in 2 parts:-
1. Apply the terraform code to set up the secret in parameter store.  Once this terraform is run, the API can be manually updated from the console

```terraform
resource "aws_ssm_parameter" "statuscake_api_key" {
  name  = "statuscake-api-key"
  type  = "SecureString"
  value = "secretvalue"

  lifecycle {
    ignore_changes = [
      value
    ]
  }
}

```

2. Apply the terraform code that sets up the statuscake provider which then sets up the health check.

```terraform
module "statuscake" {
  source = "../modules/statuscake"
  env_url_pair = {
    prod  = "https://share-life-events.service.gov.uk/health/ping"
  }
}
```
The health check page on the application is under `/health/ping` and returns a 200 on success with a response of:
```json5
{
   "status": "UP"
}
```


## Consequences
The integration of this service is two-step which greats a slightly manual process but will only be a one-off event.

[Next >>](0024-flat-file-ingestion-poc.md)
