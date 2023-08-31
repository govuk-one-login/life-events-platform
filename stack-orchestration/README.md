# Stack Orchestration Tool

## Setup

Clone the repo https://github.com/alphagov/di-devplatform-deploy in a directory next to this repo.

Copy the encryption file into `stack-orchestration` with the name `encryption_key.txt`. This file is stored in keeper.

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

## How to update

To update the parameters used for our stacks, please update the parameters in
the `configuration/[ENVIRONMENT]/[PIPELINE]/parameters.json` files.

For updating the VPC pipelines, make sure you have the encryption key file as described above, and then run
the `decrypt_vpc_parameters.sh` script. Afterwards you will be able to update the parameters. These updates will not be
tracked unless you run the `encrypt_vpc_parameters.sh` script after making changes.
