# Deployment Failures

## Identifying a failure?

- GitHub Actions timeout for a deployment

## How do you resolve it?

Review logs from the ECS task
- under Elastic Container Services find the affected cluster
- identify the service, e.g. `dev-gdx-data-share-poc`
-- at a service level you can see logs for all instances, you can also find logs for specific pending instances
