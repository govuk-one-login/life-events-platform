import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  id("org.owasp.dependencycheck") version "8.0.1"
  id("org.springframework.boot") version "2.7.8"
  id("io.spring.dependency-management") version "1.1.0"
  id("org.jmailen.kotlinter") version "3.13.0"
  kotlin("jvm") version "1.8.0"
  kotlin("plugin.spring") version "1.8.0"
}

group = "uk.gov.gds"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-hateoas")

  //  AWS dependencies for SNS, SQS etc
  implementation(platform("software.amazon.awssdk:bom:2.19.21"))
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.1.0")
  implementation("software.amazon.awssdk:rds")

  implementation("org.springframework:spring-jms")
  implementation("org.hibernate:hibernate-validator:8.0.0.Final")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
  implementation("org.apache.commons:commons-csv:1.9.0")

  runtimeOnly("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("software.amazon.jdbc:aws-advanced-jdbc-wrapper:1.0.0")
  implementation("org.postgresql:postgresql:42.5.1")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.14")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.14")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.1")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("commons-codec:commons-codec")
  implementation("commons-net:commons-net:3.9.0")

  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("io.micrometer:micrometer-core:1.10.3")
  implementation("io.micrometer:micrometer-registry-cloudwatch2:1.10.3")
  implementation("io.opentelemetry:opentelemetry-api:1.22.0")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.1.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.1.0")

  implementation("com.toedter:spring-hateoas-jsonapi:1.6.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.11")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.testcontainers:localstack:1.17.6")
  testImplementation("org.testcontainers:postgresql:1.17.6")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("io.mockk:mockk:1.13.3")
  testImplementation("org.mockito:mockito-inline")
  testImplementation("org.mockito:mockito-junit-jupiter:5.0.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
  testImplementation("org.mockito:mockito-inline:5.0.0")
  testImplementation("com.approvaltests:approvaltests:18.5.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}
repositories {
  mavenCentral()
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }
  }
  withType<Test> {
    useJUnitPlatform()
  }
}

springBoot {
  buildInfo()
}
version = getVersion()

fun getVersion(): String {
  return if (System.getenv().contains("BUILD_NUMBER")) System.getenv("BUILD_NUMBER")
  else LocalDate.now().format(DateTimeFormatter.ISO_DATE)
}
