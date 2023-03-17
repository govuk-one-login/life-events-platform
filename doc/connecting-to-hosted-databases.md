# Connecting to hosted databases

We use RDS databases for our hosted environments. For security reasons these are not accessible from the public
internet. This makes connecting to them for debugging purposes slightly tricky. To enable this, we have set up a bastion
EC2 instance per environment, which is in the same VPC as the database and therefore allowed to connect to it. You can
connect to this EC2 instance via AWS Systems Manager Session Manager.

## SSM tunneling

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

## Connecting to the EC2 instance via the AWS console

It's also possible to connect to the database via the AWS console. Log into the console, and go to the EC2 console.
Choose instances, and select the instance you would like to connect to. At the top, click the connect button, and go to
the Session Manager tab. Click connect and it should open a terminal.

If this is the first time the bastion has been used in this way, you will need to install psql with

```shell
sudo yum install postgresql
```

You can then connect to the database with

```shell
psql -h<host> -U<username> -d<database>
```

The host and database name can be found from the RDS section of the AWS console, while the username and password (
entered interactively) can be found as `rds_username` and `rds_password` as above.
