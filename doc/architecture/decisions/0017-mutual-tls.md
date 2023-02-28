# 17. mutual TLS (mTLS)
[Next >>](9999-end.md)

Date: 2023-02-28

## Status

Proposal for discussion

## Context

Some acquirers and data suppliers have a desire for additional security measures over and above encryption in transit
and the OIDC authentication currently in place, as part of a Zero Trust approach.


mTLS only really has benefits when it's applied everywhere, but we're not currently in a position to change the
API surface enough to effectively use mTLS.

## Options

We can potentially handle mTLS in the Spring/Kotlin service layer, but this adds significant complexity.
Alternatively, AWS API Gateway offers management of mTLS.

API Gateway doesn't support CRLs natively, which might need a custom lambda to manage, additionally
we need a way to feed back the nature of the certificate to the upstream service, see AWS [blog](
https://aws.amazon.com/blogs/compute/propagating-valid-mtls-client-certificate-identity-to-downstream-services-using-amazon-api-gateway/).

## Decision

For now, don't implement mTLS, when appropriate we should

- add an AWS API Gateway to handle mTLS termination
- use AWS ACP Private CA to manage certificate issuing
- enable mTLS

We should ensure this configuration can be managed at an environment level to facilitate easy onboarding for clients
avoiding mTLS if not required. Additionally, we'll want to issue certs etc in response to an API call to speed up
onboarding clients and giving them easy access to trial ideas.

## Impact

- all client calls will be authenticated with mTLS in addition to the OIDC / Cognito auth
- this doesn't cover queues and related other outputs, only the API surface

[Next >>](9999-end.md)

