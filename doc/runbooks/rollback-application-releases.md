# Recovering from application releases


<table>
  <tr>
   <td>Description
   </td>
   <td>This document covers our processes for dealing with a deployment that need to be rolled-back due to application errors.
   </td>
  </tr>
  <tr>
   <td>Collaborators
   </td>
   <td><em>Michael Willis, Oskar Williams</em>
   </td>
  </tr>
  <tr>
   <td>Date
   </td>
   <td><em>14/6/23</em>
   </td>
  </tr>
  <tr>
   <td>Status
   </td>
   <td><em>In progress</em>
   </td>
  </tr>
</table>



# Context

When deploying a new version of software, it's important to minimise the risk of downtime, errors, and user dissatisfaction. But sometimes things go wrong, and it's necessary to have a plan to revert to a previous state.

Running a live service requires regular patching, bug fixes and feature improvements.  Ultimately this means that sometimes a problem with the running service can occur as a result of a new release.  Mitigation of these issues can be achieved with good test coverage and end to end smoke testing - but there is no substitute for a live environment highlighting something new or misconfigured.

This document looks at the approach to perform this action.  Two common strategies to do this are deployment **rollback** and **blue/green** deployment.


### Blue/green deployment

Blue/green deployment is a more advanced way to switch between versions. It means having two identical environments, one with the current version (blue) and one with the new version (green). You can test the green environment before making it live, and then switch the traffic from blue to green with a load balancer or a DNS change.


#### Advantages

Zero-downtime deployment and easy rollback


#### Disadvantages

Requires more resources, coordination, and synchronisation.


### Deployment rollback

Deployment rollback is the simplest way to undo a deployment. It means restoring the previous version of your software from a backup or a source control repository.


#### Advantages

Fast and easy to execute.


#### Disadvantages

Can cause data loss, inconsistency, or corruption if the new version has made any changes to the database or other external resources.


# Decision

Although larger and more complex systems may benefit from blue/green deployment due to its reduced risk of errors and data issues, more frequent and urgent deployments favour deployment rollback, as it is quicker and simpler to execute. The simplicity of rollback was chosen as the approach.


# Approach for rollback

Two approaches were considered:



1. Use a Git revert, and merge.
2. Manually revert a code deploy.

Both options have validity, option 1 being simpler and clearer what has happened, but option 2 is potentially faster and less error prone.  Option 2 was considered the standard approach to use when performing production rollbacks.

**Steps:**



* Re-run the previous CodeDeploy for the environment.
* This reverts the environment back one definition
* In order to correct the most recent task definition, re-run the pipeline for the commit that it references.


# Consequences


#### Issues

Care will need to be taken when reverting db changes.


#### Resolution

All db changes will need to be backwards compatible, meaning that for the first few releases the database will work with either versions of the code.  Once the system has “stabilised” the backward compatibility can be removed.


# References

_[Redeploy and roll back a deployment with CodeDeploy](https://docs.aws.amazon.com/codedeploy/latest/userguide/deployments-rollback-and-redeploy.html) _

_[Automate rollbacks for Amazon ECS rolling deployments with CloudWatch alarms | Containers](https://aws.amazon.com/blogs/containers/automate-rollbacks-for-amazon-ecs-rolling-deployments-with-cloudwatch-alarms/)_
