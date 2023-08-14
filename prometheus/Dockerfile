FROM public.ecr.aws/aws-observability/aws-otel-collector:v0.32.0 as base

FROM alpine:3.18.3 as run

## Adds envsubst
ENV BUILD_DEPS="gettext"  \
    RUNTIME_DEPS="libintl"

RUN set -x && \
    apk add --no-cache --update $RUNTIME_DEPS && \
    apk add --no-cache --virtual build_deps $BUILD_DEPS &&  \
    cp /usr/bin/envsubst /usr/local/bin/envsubst && \
    apk del build_deps

ENV RUN_IN_CONTAINER=True
ENV HOME=/root

COPY --from=base /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
COPY --from=base /awscollector /awscollector
COPY --from=base /etc/ /etc/
COPY --from=base /healthcheck /healthcheck

COPY adot-config.template.yaml /etc/ecs/adot-config.template.yaml
COPY start_collector.sh /etc/ecs/start_collector.sh

RUN chmod +x /etc/ecs/start_collector.sh

EXPOSE 2000/tcp
EXPOSE 4317/tcp
EXPOSE 55681/tcp

ENTRYPOINT ["/etc/ecs/start_collector.sh"]
