## Application Recovery

With reference to [ADR 0027](../architecture/decisions/0027-recovery-and-rollback-strategy.md) for rollback strategy

### Code release rollback

Use these steps to roll back to the previous application version:-

1. Go into the CodeDeploy in the AWS console.
2. Find the previous deployment and click into it.
3. Click retry deployment, this will cause the deployed version to be rolled back.
4. Fix and merge new code into git.

### DB migration recovery

The DB migration rollback will actually be a roll-forward.

The following steps should be followed:-

1. Commit a new migration that fixes the db.
2. Fix and merge new code into git.
