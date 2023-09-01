[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=alphagov_di-data-life-events-platform&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=alphagov_di-data-life-events-platform)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=alphagov_di-data-life-events-platform&metric=coverage)](https://sonarcloud.io/summary/new_code?id=alphagov_di-data-life-events-platform)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=alphagov_di-data-life-events-platform&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=alphagov_di-data-life-events-platform)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=alphagov_di-data-life-events-platform&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=alphagov_di-data-life-events-platform)

# GDX Data Share Platform

Cross governmental data sharing platform, to simplify data acquisition and sharing in an event driven way.

## Documentation

Techdocs for this service are available [here](https://alphagov.github.io/di-data-life-events-platform/).
There are also [Swagger docs](https://dev.share-life-events.service.gov.uk/swagger-ui.html).

## Running the service

### Running locally

See [contributing](CONTRIBUTING.md) for more info on running the service locally for development.

For running locally against docker instances of the following services:

- run this application independently e.g. in IntelliJ

`docker-compose -f docker-compose-local.yml up`

### Running all services including this service

`docker-compose up`

### Running remotely

The service is deployed to AWS, accessible through

| environment | url                                                                 |
|-------------|---------------------------------------------------------------------|
| dev         | https://dev.share-life-events.service.gov.uk/swagger-ui/index.html  |
| demo        | https://demo.share-life-events.service.gov.uk/swagger-ui/index.html |
| prod        | https://share-life-events.service.gov.uk/swagger-ui/index.html      |

## Working with SAM

### Setup

To set up sam and be able to run it in the correct environment, run the following commands:

Run these

```shell
brew install jenv
echo '# Add jenv to PATH and initialise' >> ~/.zshrc
echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.zshrc
echo 'eval "$(jenv init -)"' >> ~/.zshrc
echo '' >> ~/.zshrc

brew install aws-sam-cli

brew tap homebrew/cask-versions
brew install corretto17
```

Restart terminal then run

```shell
jenv add /Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home/
jenv global corretto64-17.0.8.1
```

Restart terminal then run in the project top directory

```shell
sam build
```

### Deploying

To deploy our SAM to the dev environment, please create your own configuration and then apply with that. To create your
own configuration, modify and add the following to `samconfig.toml`. This should then be committed and tracked in our
git, so that we maintain a history of these stacks.

```ini
[dev-<your-name>.deploy.parameters]
capabilities = "CAPABILITY_NAMED_IAM"
confirm_changeset = true
resolve_s3 = true
region = "eu-west-2"
stack_name = "<your-name>-lep"
s3_prefix = "<your-name>-lep"
parameter_overrides = "Environment=\"dev\" VpcStackName=\"ia-Vpc-E5N71SHB6HRJ\" Developer=\"<your-name>\""
```

Once you have added this, you can then run the below while logged into the dev environment via SSO to deploy to dev:

```shell
sam build
sam deploy --config-env dev-<your-name>
```

## Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)

## Glossary

See [TechDocs](https://alphagov.github.io/di-data-life-events-platform/glossary.html).

## Licence

[MIT License](LICENCE)
