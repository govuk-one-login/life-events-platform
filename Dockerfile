FROM eclipse-temurin:19-jre-jammy AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew .
COPY gradle/ gradle/
RUN ./gradlew build || return 0


ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

COPY . .
RUN ./gradle clean assemble -Dorg.gradle.daemon=false

FROM eclipse-temurin:19-jre-jammy
LABEL maintainer="GDX Vison <info@gds.gov.uk>"

RUN apt-get update && \
    apt-get -y upgrade && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Install AWS RDS Root cert into Java truststore
RUN mkdir /home/appuser/.postgresql
ADD --chown=appuser:appgroup https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem /home/appuser/.postgresql/root.crt

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/gdx-data-share-poc*.jar /app/app.jar

USER 2000

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-jar", "/app/app.jar"]
