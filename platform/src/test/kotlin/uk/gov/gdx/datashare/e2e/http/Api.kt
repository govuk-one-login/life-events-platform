package uk.gov.gdx.datashare.e2e.http

import com.fasterxml.jackson.module.kotlin.readValue
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.*
import uk.gov.gdx.datashare.repositories.Acquirer
import uk.gov.gdx.datashare.repositories.AcquirerSubscription
import uk.gov.gdx.datashare.repositories.Supplier
import uk.gov.gdx.datashare.repositories.SupplierSubscription
import uk.gov.gdx.datashare.e2e.Config
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

class Api {
  private val authorization = "Bearer ${getAdminAuthToken()}"
  private val client = HttpClient.newHttpClient()
  private val baseRequest = HttpRequest.newBuilder().header("Authorization", authorization)
  private val objectMapper = JacksonConfiguration().objectMapper()

  fun createSupplierWithCognitoClient(clientName: String): CognitoClientResponse = postRequest(
    "/admin/supplier",
    CreateSupplierRequest(
      clientName = clientName,
      eventType = EventType.TEST_EVENT,
    ),
  )

  fun getSuppliers(): List<Supplier> = getRequest("/suppliers")
  fun getSupplierSubscriptions(): List<SupplierSubscription> =
    getRequest("/suppliers/subscriptions")

  fun getSupplierSubscriptionsForSupplier(supplierId: UUID): List<SupplierSubscription> =
    getRequest("/suppliers/$supplierId/subscriptions")

  fun deleteSupplier(supplierId: UUID) = deleteRequest("/suppliers/$supplierId")

  fun createAcquirerWithCognitoClient(clientName: String): CreateAcquirerResponse = postRequest(
    "/admin/acquirer",
    CreateAcquirerRequest(
      acquirerName = clientName,
      eventType = EventType.TEST_EVENT,
      enrichmentFields = listOf(EnrichmentField.POSTCODE),
    ),
  )

  fun createAcquirerWithQueue(clientName: String, queueName: String, principalArn: String): CreateAcquirerResponse =
    postRequest(
      "/admin/acquirer",
      CreateAcquirerRequest(
        acquirerName = clientName,
        eventType = EventType.TEST_EVENT,
        enrichmentFields = listOf(EnrichmentField.POSTCODE),
        queueName = queueName,
        principalArn = principalArn,
      ),
    )

  fun getAcquirers(): List<Acquirer> = getRequest("/acquirers")
  fun getAcquirerSubscriptions(): List<AcquirerSubscription> =
    getRequest("/acquirers/subscriptions")

  fun getAcquirerSubscriptionsForAcquirer(acquirerId: UUID): List<AcquirerSubscriptionDto> =
    getRequest("/acquirers/$acquirerId/subscriptions")

  fun deleteAcquirer(acquirerId: UUID) = deleteRequest("/acquirers/$acquirerId")

  private inline fun <reified O> getRequest(relativePath: String): O {
    val request = baseRequest
      .uri(URI.create(Config.apiUrl + relativePath))
      .GET()
      .build()

    val response = client.send(
      request,
      HttpResponse.BodyHandlers.ofString(),
    )

    return objectMapper.readValue<O>(response.body())
  }

  private inline fun <I, reified O> postRequest(relativePath: String, body: I): O {
    val requestBody = objectMapper.writeValueAsString(body)

    val request = baseRequest
      .uri(URI.create(Config.apiUrl + relativePath))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()

    val response = client.send(
      request,
      HttpResponse.BodyHandlers.ofString(),
    )

    return objectMapper.readValue<O>(response.body())
  }

  private fun deleteRequest(relativePath: String) {
    val request = baseRequest
      .uri(URI.create(Config.apiUrl + relativePath))
      .DELETE()
      .build()

    client.send(
      request,
      HttpResponse.BodyHandlers.ofString(),
    )
  }
}
