# Making production changes

## Routine changes

For routine configuration changes, the API endpoints should be used. If an endpoint doesn't exist, work to create it
should be prioritised. This reduces the chances of errors, and ensures consistency. The following tasks are carried out
via the API:

- Adding a new acquirer
- Changing the data an acquirer receives
- Removing an acquirer
- Adding a supplier
- Removing a supplier

These actions are performed using named accounts for auditing purposes. API calls should be paired on to reduce errors,
and performed by two developers with appropriate clearance from a GDS issued device. A Jira ticket should be logged for
each configuration change.

## Exceptional changes

The following methods for making changes to a production system are listed in order of preference. A method should only
be used when any more favourable alternatives are not feasible.

1. Code changes, peer reviewed, and deployed using CI
2. A script invoking the AWS CLI or similar, peer reviewed, run while pairing and using an assumed role
3. Manual action using the AWS Console, while pairing, using an assumed role, and carefully documenting every action taken
4. A script invoking the AWS CLI or similar, peer reviewed, run while pairing and using the root account
5. Manual action using the AWS Console, while pairing, using the root account, and carefully documenting every action taken

Any direct interaction with a production system must be made from a GDS issued device. Where possible, this is enforced
with IP restriction policies (Cognito user pool IP restrictions, AWS policy conditions).
All accounts with access to production systems are protected by enforced MFA (conditions
on AWS policies, organisation level requirements on Github).
