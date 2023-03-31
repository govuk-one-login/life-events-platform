FROM amazoncorretto:19.0.2-alpine3.17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ gradle/
RUN ./gradlew build || return 0


ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

COPY . .
RUN ./gradlew clean assemble -Dorg.gradle.daemon=false

FROM amazoncorretto:19-alpine
LABEL maintainer="GDX Platform Team <gdx-dev-team@digital.cabinet-office.gov.uk>"

RUN apk --no-cache upgrade

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 --system appgroup && \
    adduser --u 2000 --system appuser 2000

# Install AWS RDS Root cert into Java truststore
RUN mkdir /home/appuser/.postgresql
ADD --chown=appuser:appgroup https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem /home/appuser/.postgresql/root.crt

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/gdx-data-share-poc*.jar /app/app.jar

USER 2000

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-jar", "/app/app.jar"]
