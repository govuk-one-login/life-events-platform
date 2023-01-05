import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  id("org.owasp.dependencycheck") version "7.4.3"
  id("org.springframework.boot") version "2.7.4"
  id("io.spring.dependency-management") version "1.1.0"
  kotlin("jvm") version "1.8.0"
  kotlin("plugin.spring") version "1.8.0"
}

group = "uk.gov.gds"

configurations {
  implementation { exclude(module = "spring-boot-starter-web") }
  implementation { exclude(module = "spring-boot-starter-tomcat") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.337"))
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.1.0")
  implementation("com.amazonaws:aws-java-sdk-sns")
  implementation("org.springframework:spring-jms")
  implementation("org.hibernate:hibernate-validator:8.0.0.Final")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.apache.commons:commons-csv:1.9.0")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:r2dbc-postgresql")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.5.1")

  implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.14")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.14")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("io.opentelemetry:opentelemetry-api:1.21.0")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("commons-codec:commons-codec")
  implementation("commons-net:commons-net:3.9.0")
  implementation("com.google.code.gson:gson")

  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("com.amazonaws:aws-java-sdk-s3:1.12.376")

  implementation("io.micrometer:micrometer-core:1.10.2")
  implementation("io.micrometer:micrometer-registry-cloudwatch2:1.10.2")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.0.1")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.0.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.assertj:assertj-core:3.23.1")
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.mockito:mockito-inline")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.9")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.testcontainers:localstack:1.17.6")
  testImplementation("org.testcontainers:postgresql:1.17.6")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("io.mockk:mockk:1.13.3")
  testImplementation("org.mockito:mockito-junit-jupiter:4.8.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
  testImplementation("org.mockito:mockito-inline:4.8.1")
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
      jvmTarget = "18"
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
