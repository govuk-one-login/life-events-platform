# Stack Orchestration Tool

## Setup

Clone the repo https://github.com/alphagov/di-devplatform-deploy in a directory next to this repo.

### Required CLIs

To run this tool you will need the below CLI's

* aws cli for managment of Cloudformation stacks
* aws sso for authentication
* jq for formatting and conversion

## How to use

Choose your account to run against. Login to this account with AWS SSO, setting your AWS_PROFILE environment variable,
then run the below, replacing `<environment>`with one of `dev`, `build`, `staging`, `integration`, `production`:

```shell
./provision_all.sh <environment>
```
