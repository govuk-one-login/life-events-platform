import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  id("org.springframework.boot") version "3.0.4"
  id("io.spring.dependency-management") version "1.1.0"
  id("org.jmailen.kotlinter") version "3.13.0"
  id("org.springdoc.openapi-gradle-plugin") version "1.6.0"
  kotlin("jvm") version "1.8.10"
  kotlin("plugin.spring") version "1.8.10"
  id("com.netflix.nebula.jakartaee-migration") version "0.1.9"
}

jakartaeeMigration {
  migrate()
}

openApi {
  outputFileName.set("openapi.json")
  customBootRun.args.set(listOf("--spring.profiles.active=dev,localstack"))
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
  implementation("org.springframework.boot:spring-boot-starter-aop")

  //  AWS dependencies for SNS, SQS etc
  implementation(platform("software.amazon.awssdk:bom:2.20.23"))
  implementation("software.amazon.awssdk:sqs")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:2.0.3")
  implementation("software.amazon.awssdk:rds")
  implementation("software.amazon.awssdk:cognitoidentityprovider")
  implementation("software.amazon.awssdk:ssm")
  implementation(platform("com.amazonaws:aws-xray-recorder-sdk-bom:2.13.0"))
  implementation("com.amazonaws:aws-xray-recorder-sdk-spring")
  implementation("com.amazonaws:aws-xray-recorder-sdk-aws-sdk-v2")
  implementation("com.amazonaws:aws-xray-recorder-sdk-slf4j")

  implementation("org.springframework:spring-jms")
  implementation("org.hibernate:hibernate-validator:8.0.0.Final")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

  runtimeOnly("org.flywaydb:flyway-core")

  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("software.amazon.jdbc:aws-advanced-jdbc-wrapper:1.0.1")
  implementation("org.postgresql:postgresql:42.5.4")

  implementation("org.springdoc:springdoc-openapi-starter-common:2.0.3")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.3")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.0.3")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  implementation("io.micrometer:micrometer-core:1.10.5")
  implementation("io.micrometer:micrometer-registry-prometheus:1.10.4")
  implementation("io.opentelemetry:opentelemetry-api:1.24.0")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.2.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.2.0")

  implementation("com.toedter:spring-hateoas-jsonapi:2.0.3")

  implementation("org.javers:javers-spring:6.11.0")
  implementation("org.javers:javers-persistence-sql:6.11.0")

  // test containers
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("com.amazonaws:aws-java-sdk-core:1.12.426") // required for TestContainers https://github.com/testcontainers/testcontainers-java/issues/1442#issuecomment-694342883

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.12")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("io.mockk:mockk:1.13.4")
  testImplementation("com.approvaltests:approvaltests:18.5.0")
  testImplementation("com.google.code.gson:gson:2.10.1") // Needed for JsonApprovals
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
