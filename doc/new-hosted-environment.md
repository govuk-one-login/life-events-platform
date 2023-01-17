# Creating a new hosted environment
## Terraform
todo
## Database
Once the infrastructure has been created using terraform, a database user needs to be created.
Connect to the RDS cluster using the bastion ([instructions](./connecting-to-hosted-databases.md)) and create the user:
```psql
CREATE USER <username> WITH LOGIN;
GRANT rds_iam TO <username>;
GRANT rds_superuser TO <username>;
```

The `<username>` needs to be set as a module varible in terraform, e.g. `terraform/dev/main.tf` > `module "data-share-service"` > `db_username`
