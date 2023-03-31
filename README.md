# GDX Data Share Platform

Cross governmental data sharing platform, to simplify data acquisition and sharing in an event driven way.

## Documentation
Techdocs for this service are available [here](https://alphagov.github.io/gdx-data-share-poc/).
There are also [Swagger docs](https://d33v84mi0vopmk.cloudfront.net/swagger-ui.html).

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

There are numerous terms and acronyms used in this codebase that aren't immediately obvious, including

| Term     | Definition                                                                                          |
|----------|-----------------------------------------------------------------------------------------------------|
| GDS      | Government Digital Service - https://www.gov.uk/government/organisations/government-digital-service |
| GDX      | Government Data Exchange - This project and the wider programme of work                             |
| DWP      | Department for Work and Pensions                                                                    |
| LEN      | Life Event Notification (a service from HMPO)                                                       |
| HMPO     | HM Passport Office                                                                                  |
| HMPPS    | HM Prison and Probation Service, and executive agency of the MoJ                                    |
| MOJ      | Ministry of Justice                                                                                 |
| GRO      | General Registry Office                                                                             |
| OGD      | Other Government Department                                                                         |
| TUO      | Tell us once - https://www.gov.uk/after-a-death/organisations-you-need-to-contact-and-tell-us-once  |
| Acquirer | A downstream department or service consuming events from our API (for example DWP)                  |
| Supplier | An upstream department or service GDX consumes events and data from (for example HMPO)              |

## Licence
[MIT License](LICENCE)
