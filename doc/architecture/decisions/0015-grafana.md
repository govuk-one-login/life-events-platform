# 15. Grafana

[Next >>](9999-end.md)

Date: 2023-02-23

## Status

Draft

## Context

The metrics for our system has been migrated from AWS CloudWatch to Amazon Managed Services for Prometheus (AMP). This
was due to the fact that CloudWatch's concept of metrics and dimensions is lacking, it treats dimensions as just more
metrics. The result of using AMP is that we need to have a system for dashboards and exploring these metrics, so we
decided to use Grafana.

There are 2 ways we could use Grafana, either through Amazon Managed Services for Grafana (AMG) or through our own
self-hosted Grafana instance, hosted in ECS.

## Decision

Our current decision is to use our own self-hosted Grafana rather than AMG.

AMG as a system has several disadvantages that caused us to go this way:

1. AMG only allows for auth via IdPs that support SAML or AWS IAM Identity Center. This would require us to use
   organisation level IdPs that would require maintenance and oversight from higher up than the GDX team, not currently
   reasonable in our fast-paced iterative approach to development.
2. AMG has restrictive configuration, preventing us from being able to edit and update much of the config that we can
   through our own self-hosted Grafana

In contrast, the self-hosted Grafana does require some initial set up and continued maintenance, however this is
expected to be fairly minor and easy to complete. Also, the self-hosted Grafana still has all the necessary access to
our AMP for things like alert silencing and monitoring (for alerts defined in our AMP), and allows for a much greater
flexibility if we desire in the future to add in more complex configuration.

## Future risks

The major risks here are the maintenance of the self-hosted Grafana instance that may be performed by AMG automatically.
We expect this to be low risk and cost, as we expect any major breaking updates to require our input for AMG anyway, and
for minor updates the maintenance should be trivial.

[Next >>](9999-end.md)

