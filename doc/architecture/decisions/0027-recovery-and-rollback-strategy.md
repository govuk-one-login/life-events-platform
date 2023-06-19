# 27. Recovery and Rollback Strategy

[Next >>](9999-end.md)

Date: 2023-06-18

## Status

Accepted

## Context

When deploying a new version of software, it's important to minimise the risk of downtime, errors, and user
dissatisfaction. But sometimes things go wrong, and it's necessary to have a plan to revert to a previous state.

Running a live service requires regular patching, bug fixes and feature improvements. Ultimately this means that
sometimes a problem with the running service can occur as a result of a new release. Mitigation of these issues can be
achieved with good test coverage and end to end smoke testing, we have all of these in place, - but there is no
substitute for a live environment highlighting something new or misconfigured.

In our deployment pipeline, we use CodeDeploy to manage automatic blue/green deployments, with integrated smoke testing.
We also have 2 types of deployments, code/service deployments and database migrations. Importantly, we will not deploy
any changes that both alter the database and alter the code, as this allows us to more easily rollback any changes that
pass deployment but end up needing to be fixed.

## Decision

Our 2 recovery processes are designed to have minimum downtime.

For code, we will simply redeploy the most recent version, by retrying the deployment in CodeDeploy, to fix the running
service. After this, we will then continue as usual, and the next released code will fix the issue spotted in the
failing code.

For database migrations, we will not perform any manual changes to our databases, but we will simply write a fixing
migration in our code, and deploy that with a git merge and then the deployment pipeline. This is in essence a
roll-forward.

## Consequences

To stick to this approach, care must be taken to make sure we do not have releases that contain both an update to the
code and to the database.

[Next >>](9999-end.md)
