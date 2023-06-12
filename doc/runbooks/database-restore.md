# Database restore

There are several types of database backups:

- Daily snapshots
- Daily cross-region snapshots
- Point in time backups

All of these have the same restore procedure. Restoring will result in around 15-30 minutes of system downtime.

## Restore procedure
As with any live system change, this should be paired on if carried out in production.

### Create a new database instance from the target image
Using the AWS console, locate the relevant image and use the wizard to create a new database cluster from it. The daily
and cross-region daily snapshots can be found on the snapshots page, while the point-in-time options are available from
the actions menu of the selected database.

When naming the cluster and instance, add the `-new` suffix to the original names, for example `demo-rds-db-new` and
`demo-rds-db-az1-new`. The cluster will be created with only once instance. This is fine as the other instances will be
created later. All other settings can be left at default.

Wait for this to complete.

### Rename the database cluster and instances

This step will cause the application to become unavailable.

If they exist, rename the existing cluster and instances by suffixing them with `-old`. For example `demo-rds-db-old`.
Apply this change immediately, not during the next maintenance window.

Once this has started, the new cluster and instance should be renamed by removing the `-new` from the name.

Wait for these renames to complete

### Terraform apply

This step will bring the application back up.

Run a terraform apply against the target environment by running the `Infrastructure - Main` job in github actions.
The application will come back up fairly quickly, but the apply will take some time as it creates additional instances.

It may be necessary to run the apply twice, as it can timeout on the first attempt. This is normal.

### Clean up
Once you are satisfied that the restore was successful, clean up by deleting the old cluster and instances. The reader
instances must be deleted first, followed by writers.

After the instances are deleted, check Security Hub for any findings which need suppressing.
