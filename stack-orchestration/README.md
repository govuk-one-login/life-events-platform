# Stack Orchestration Tool

## Setup

Clone the repo https://github.com/alphagov/di-devplatform-deploy in a directory next to this repo.

Copy each of the configuration/<env>/vpc/parameters.json.templates files to parameters.json and replace <SFTP_IP> with
the IP address of the GRO SFTP server for that environment. GRO will provide these during initial project setup, but
they should subsequently be stored in SSM Parameter Store in the corresponding AWS account (they are required for the
gro-pull-file lambda).

Do not commit the SFTP server IP address!

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
