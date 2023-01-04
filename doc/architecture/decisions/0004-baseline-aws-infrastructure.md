# 4. Service will be deployed to AWS, using GitHub actions and Terraform

[Next >>](0005-queue-api.md)


Date: 2022-12-01

## Status

Accepted

## Context

Aiming to deploy existing codebase to a "real" environment to enable faster future development and demonstrate the baseline system functioning.

## Architecture

Following the same architecture as [ADR 3](0003-gdx-death-notification-event.md), deployed to a real AWS environment
![This is the Death event architecture{arch}](death-event-notifier.svg)

### Source Control

This GitHub repo will be the source for all application and infrastructure code

### CI/CD tooling

GitHub actions will be used as the deployment pipeline, provides good visibility, infrastructure in code and ease of iteration

### Infrastructure as Code

Using Terraform as a well support IaC tool, with [tfsec](https://github.com/aquasecurity/tfsec) to enforce best practice security.
A single environment will be used for now, but will need to support the idea of multiple environments in future.

### Container runtimes

The current solution has a single container serving multiple purposes. For simplicity, deploy into ECS using an ECR for image storage. 
This may be iterated on in future (e.g. Lambdas, or EKS) but for rapid deployment and simplicity, will use ECS initially.

## Decision

This approach follows the agreed pattern of architecture for GDX


[Next >>](0005-queue-api.md)
