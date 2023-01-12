# Connecting to hosted databases
We use RDS databases for our hosted environments. For security reasons these are not accessible from the public internet.
This makes connecting to them for debugging purposes slightly tricky. To enable this, we have set up a bastion EC2 instance
per environment, which is in the same VPC as the database and therefore allowed to connect to it. You can connect to this
EC2 instance via SSH.

## Getting SSH keys
It may be easier to get these from another member of the team. If not, or for a new environment:

You will need the terraform cli, and AWS access keys configured with access to the S3 backing store used for the environment
you are trying to connect to. These keys should be in `~/.aws/credentials` as normal.

 - `cd` into `./terraform/{environment}` and run `terraform show -json > state.json` to dump out a state file.
 - Open this file in IntelliJ or another IDE and format it. Search for `private_key_openssh` and `public_key_openssh`.
IntelliJ should replace the `\n` literals in the file with new lines, but if not you'll need to do this in the private key
 - Copy these values to e.g. `~/.ssh/id_bastion_{env}` and `~/.ssh/id_bastion_{env}.pub` respectively
 - If on a *nix system, ensure they have the correct file permissions (`chmod 600 ~/.ssh/id_bastion_{env}`)
 - Delete the state.json file from earlier

## Connecting to the bastion
For small changes, it may be easier to just SSH into the bastion and connect to the database from there. For this:
```shell
ssh ec2-user@<ip> -i ~/.ssh/id_bastion_env
```
where <ip> is the IP address of the bastion host. You can get this from looking at EC2 instances in the AWS console.
If this is the first time the bastion has been used in this way, you will need to install psql with
```shell
sudo yum install postgresql
```
You can then connect to the database with
```shell
psql -h<host> -U<username> -d<database>
```
The host and database name can be found from the RDS section of the AWS console, while the username and password (entered interactively)
can be found as `rds_username` and `rds_password` in the state file from getting the SSH keys.

## SSH tunneling
For bigger tasks, it would be more convenient to be able to access the database directly from your development machine.
To achieve that, we can tunnel the connection through the bastion host. IntelliJ has built in support for this, and we can
also do it manually.

### IntelliJ Database connection
In the database tab, set up a new Postgres datasource. Under the SSH/SSL tab, tick `Use SSH tunnel` and create a new
configuration. The host should be the IP of the bastion (from the EC2 section of the AWS console), the username is
`ec2-user`, and you should select Key pair authentication type with the private key file you created earlier.

The host and database name can be found from the RDS section of the AWS console, while the username and password (entered interactively)
can be found as `rds_username` and `rds_password` in the state file from getting the SSH keys.

### Manual tunneling
Set up a tunnel with
```shell
ssh -i <path to private key from above> -N -L 5433:<db hostname>:5432 ec2-user@<bastion ip>
```

This will make the database accessible on localhost:5433 and you can interact with it as normal.
