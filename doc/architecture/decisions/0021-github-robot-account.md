# 21. GitHub robot account

[Next >>](0022-allow-application-to-manage-acquirer-queues.md)

Date: 2023-04-04

## Status

Draft for discussion

## Context

We want to find and remediate any vulnerabilities in our dependencies, so we want to have their versions pinned down.
The gradle build file pins this for us for our top level dependencies, but in order to pin our transient
dependencies we need to use a gradle lockfile.

Sadly dependabot can only handle updating the gradle build file, not the lockfile, if this changes we can look back at
this and possibly remove our robot account.

This means that we need a way to update the lockfile in our dependabot pull requests. This is possible with actions,
however these actions do not then trigger subsequent actions unless they are made with a Personal Access Token (PAT) of
a GitHub account. As a consequence we need to set up a robot account to have a PAT that we can use.

## Approach

We have created an account `gdx-dev-team` linked to out group email
address, `gdx-dev-team@digital.cabinet-office.gov.uk`. This will be our robot account. We will use the PAT for this
account in the workflow to update the gradle lockfile.

## Consequences

As a result of this we will have another account with access to our repo and actions, but this will be carefully
managed.

As an additional benefit we can now have the auto formatting actions use this PAT as well, allowing for the contributors
not needing to recommit any changes it makes like they currently do (normally through squashing).

[Next >>](0022-allow-application-to-manage-acquirer-queues.md)
