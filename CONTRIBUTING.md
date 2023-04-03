# Contributing

## Onboarding as a developer

- request access for GitHub and AWS Console
- make sure MFA is enabled on AWS Console account

## Local Development Setup

Requirements
- Java 20 (Coretto recommended) for local running, test running

Generally, development is easier with
- IntelliJ
- Docker, Docker Compose
and these instructions are based on that

For a new configuration

![image](https://user-images.githubusercontent.com/8627917/208089984-cfbc5048-702b-40da-a05a-e3d0e0e9e89f.png)

- From run/debug configurations, create a new Spring Boot configuration
- under "Runs on" select "Docker Compose" under "Create New Target"
- select  `docker-compose.yml` from the root of the repository as the configuration file
- select `gdx-data-share-poc` as the service

- If you have issues running like not being able to find localstack, manually spinning up services may help. In a terminal, run
  ```
  docker-compose up datashare-db oauth2 localstack -d
  ```
