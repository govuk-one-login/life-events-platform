# Monitoring

## Our setup

Our monitoring setup is below

```mermaid
flowchart TB
    subgraph Env1 [Environment 1]
        subgraph A1 [ECS Task A]
            GDXB1[GDX]-->ADOTA1[ADOT]
        end
        subgraph B1 [ECS Task B]
            GDXA1[GDX]-->ADOTB1[ADOT]
        end
        ADOTA1 --> AMP1
        ADOTB1 --> AMP1
    end
    subgraph Env2 [Environment 2]
        subgraph A2 [ECS Task A]
            GDXB2[GDX]-->ADOTA2[ADOT]
        end
        subgraph B2 [ECS Task B]
            GDXA2[GDX]-->ADOTB2[ADOT]
        end
        ADOTA2 --> AMP2
        ADOTB2 --> AMP2
    end
    AMP1 --> Grafana[Self-hosted Grafana]
    AMP2 --> Grafana[Self-hosted Grafana]
```

Each of the ECS tasks contains both our service (GDX) container and an AWS Distro for Open Telemetry (ADOT) container.

The ADOT container has a Prometheus scraper that scrapes the GDX container for the metrics, which it then publishes to
the Amazon Managed Services for Prometheus (AMP) for that environment.

Each AMP is then accessed from our single Self-hosted Grafana as a data source, meaning we can monitor the metrics and
alerts for all environments in one place.

## Access

To get access, please read the CONTRIBUTING doc Grafana Access section [here](../../CONTRIBUTING.md#grafana-access).
