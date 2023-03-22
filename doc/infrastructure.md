# Infrastructure

- Infrastructure is configured with a GDS AWS Account
- All infra is managed through Terraform

## Bootstrapping
- to spin up a new environment in a new AWS account, it's necessary to
    - create an S3 store for state
    - create a DynamoDB table for locks
- this can be done manually, but can also be done by using the bootstrap module.

To bootstrap with the module, create a new root terraform file (e.g. `main.tf`), reference the AWS provider and the `bootstrap` module with a unique S3 bucket and DynamboDB table, and then run it.
When the S3 bucket has been created, reference this in the backend config, e.g.
```
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
  backend "s3" {
    bucket         = "<new s3 bucket name>"
    key            = "terraform.tfstate"
    region         = "eu-west-2"
    dynamodb_table = "<new dynamodb table name>"
    encrypt        = true
  }
}
```

To enable GitHub to deploy, we also use the backend bootstrap module to configure an IAM role and federation. 
This needs to be disabled initially when creating the

This infrastructure is configured in the `backend` directory of terraform config, and the `backend-prod` folder.

## Creating a new hosted environment

### Terraform

- Create a new top level module in the terraform code (e.g. like `terraform\dev`)
- Pull in relevant configuration into it (see existing environments for available services, e.g. the lambdas for generating data may not be required)
- Add to GitHub actions, particularly [infrastructure-main.yml](.github/workflows/infrastructure-main.yml)
- Validate credentials and access
- Set up GitHub Environment rules for the relevant environment to prevent or allow automatic deploys
- If Shield Advanced is required in the environment, this needs to be enabled manually first within the account

### Database

Once the infrastructure has been created using terraform, a database user needs to be created.
Connect to the RDS cluster using the bastion ([instructions](./connecting-to-hosted-databases.md)) and create the user:
```psql
CREATE USER <username> WITH LOGIN;
GRANT rds_iam TO <username>;
GRANT rds_superuser TO <username>;
```

The `<username>` needs to be set as a module varible in terraform, e.g. `terraform/dev/main.tf` > `module "data-share-service"` > `db_username`
