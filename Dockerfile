FROM eclipse-temurin:19-jre-jammy AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

# Grab AWS RDS Root cert
RUN apt-get update && apt-get install -y curl
RUN curl https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem  > root.crt
RUN openssl x509 -outform der -in root.crt -out root.der

FROM eclipse-temurin:19-jre-jammy
LABEL maintainer="GDX Vison <info@gds.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

RUN apt-get update && \
    apt-get -y upgrade && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Install AWS RDS Root cert into Java truststore
RUN mkdir /home/appuser/.postgresql
COPY --from=builder --chown=appuser:appgroup /app/root.der /home/appuser/.postgresql/root.der
RUN keytool -import -alias rds-cert -keystore cacerts -noprompt -file /home/appuser/.postgresql/root.der -storepass changeit

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/gdx-data-share-poc*.jar /app/app.jar

USER 2000

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-jar", "/app/app.jar"]
