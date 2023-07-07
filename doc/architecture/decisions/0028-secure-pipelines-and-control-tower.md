# 28. Secure pipelines and Control tower

[Next >>](9999-end.md)

Date: 2023-06-18

## Status

Proposed

## Context

The existing platform is trying to align with the rest of DI, which uses AWS Control Tower to manage AWS accounts, and
Secure Pipelines to deploy their systems. As a result, the platform is attempting to align to this flow, while
also aligning our architecture (as laid out in [ADR 2#](Put link here)). DI also uses an environment per account flow,
which is different from our approach of an account for all our non-prod environments, and an account for our prod
environment.

AWS Control Tower integration allows for orchestrating multiple accounts while maintaining security and compliance
needs.

Secure Pipelines is a tool developed by DI that uses CodePipeline and other AWS products to deploy CloudFormation or
terraform to environments, instigated by GitHub actions.

The DI current layout of accounts is as follows:
- dev
  - this is a sandbox for development
- build
  -  this is where the CodePipeline is run
- staging
  -  this is the first automatically deployed env, where tests and checks are run before the system can be promoted
- integration
  - this and production are released at the same time, this is used for test integrations
- production
  - this and integration are released at the same time, this is the live system

The dev account is a sandbox for development.


## Decision

We have 3 stages to our migration into this new account and deployment structure:
1. Migrate the existing 2 accounts to sit under Control Tower, with our current deployment process, and request the 5 new accounts as listed above
2. Migrate the deployment for our existing system into the 5 new accounts, using Secure Pipelines for the deployment process
3. Deploy our new DI aligned architecture using Secure Pipelines in the 5 new accounts

After we have completed stage 3, we will need to clean up our 2 old accounts and decommission the old system that will be running on the 5 new accounts

[Next >>](9999-end.md)
