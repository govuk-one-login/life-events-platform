# Stack Orchestration Tool

## Setup

Clone the repo https://github.com/alphagov/di-devplatform-deploy in a directory next to this repo.

Copy the encryption file into `stack-orchestration` with the name `encryption_key.txt`. This file is stored in keeper.

### Required CLIs

To run this tool you will need the below CLI's

* aws cli for management of Cloudformation stacks
* jq for formatting and conversion

## How to use

Login into AWS with SSO on the browser. Choose an account, and select `Command line or programmatic access`. In your
terminal, run `aws configure sso` and enter the start URL and region from AWS on your browser. This will create a
profile that you can set as an environment variable, by running `export AWS_PROFILE=<profile>`.

After this you can then run the below, replacing `<environment>`with one
of `dev`, `build`, `staging`, `integration`, `production`:

```shell
./provision_all.sh <environment>
```

## How to update

To update the parameters used for our stacks, please update the parameters in
the `configuration/[ENVIRONMENT]/[PIPELINE]/parameters.json` files.

For updating the VPC pipelines, make sure you have the encryption key file as described above, and then run
the `decrypt_vpc_parameters.sh` script. Afterwards you will be able to update the parameters. These updates will not be
tracked unless you run the `encrypt_vpc_parameters.sh` script after making changes.
