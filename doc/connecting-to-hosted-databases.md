# Connecting to hosted databases

We use RDS databases for our hosted environments. For security reasons these are not accessible from the public
internet. This makes connecting to them for debugging purposes slightly tricky. To enable this, we have set up a bastion
EC2 instance per environment, which is in the same VPC as the database and therefore allowed to connect to it. You can
connect to this EC2 instance via AWS Systems Manager Session Manager.

## SSM tunneling

You must have the Session Manager plugin installed for the AWS CLI. It can be
downloaded [here](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)
.
To make a connection to the EC2 instances, run the script for the relevant environment in the scripts folder in the root
directory. These are also set up as run configurations in IntelliJ.
This will open up a connection to `localhost:5433` for the dev environment, or `localhost:5434` for the demo
environment.

### IntelliJ Database connection

In the database tab, set up a new Postgres datasource. The host is `localhost`, and the port is `5433` for dev,
or `5434`for demo.

The database name can be found from the RDS section of the AWS console.

To find the username and password, you will need the terraform cli, and AWS access keys configured with access to the S3
backing store used for the environment you are trying to connect to. These keys should be in `~/.aws/credentials` as
normal.

- `cd` into `./terraform/{environment}` and run `terraform show -json > state.json` to dump out a state file.
- Open this file in IntelliJ or another IDE and format it. Search for `rds_username` and `rds_password`.
- Delete the state.json file from earlier
