## Application Rollback


With reference to [ADR 0027](../architecture/decisions/0027-recovery-rollback-strategy.md) for rollback strategy
### Code release rollback

Use these steps to rollback to the previous application version:-

1. Go into the CodeDeploy in the AWS console.
2. Find the previous deployment and click into it
3. Click retry deployment, this will cause the deployed version to be rolled back
4. Fix and merge new code into git

### DB migration rollback
The DB migration rollback will actually be a roll-forward.

The following steps should be followed:-

1. Commit new code that fixes the db
2. Merge and release
