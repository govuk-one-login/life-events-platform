# GDX Data Share Platform

Cross governmental data sharing platform, to simplify data acquisition and sharing in an event driven way.

## Documentation
Techdocs for this service are available [here](https://alphagov.github.io/gdx-data-share-poc/).
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

| environment | url                                                                  |
|-------------|----------------------------------------------------------------------|
| dev         | https://dev.share-life-events.service.gov.uk/swagger-ui/index.html   |
| demo        | https://demo.share-life-events.service.gov.uk/swagger-ui/index.html  |
| prod        | https://share-life-events.service.gov.uk/swagger-ui/index.html       |

## Architecture

Architecture decision records start [here](doc/architecture/decisions/0001-use-adr.md)

## Glossary

See [TechDocs](https://alphagov.github.io/gdx-data-share-poc/glossary.html).

## Licence
[MIT License](LICENCE)
