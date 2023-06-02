package uk.gov.gdx.datashare.uk.gov.gdx.datashare.e2e.http

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.e2e.Config
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun getAdminAuthToken(): String {
  println(Config.apiUrl)
  println(Config.cognitoTokenUri)
  println(Config.adminClientId)
  println(Config.adminClientSecret)

  val postData = "grant_type=client_credentials&client_id=${Config.adminClientId}&client_secret=${Config.adminClientSecret}"

  val client = HttpClient.newHttpClient()
  val request = HttpRequest.newBuilder(Config.cognitoTokenUri)
    .header("Content-Type", "application/x-www-form-urlencoded")
    .POST(HttpRequest.BodyPublishers.ofString(postData))
    .build()

  val response = client.send(
    request,
    HttpResponse.BodyHandlers.ofString(),
  )

  val responseBody = ObjectMapper().readValue<AuthResponse>(response.body())
  return responseBody.accessToken
}

data class AuthResponse(
  @JsonProperty("access_token")
  val accessToken: String,
  @JsonProperty("expires_in")
  val expiresIn: Int,
  @JsonProperty("token_type")
  val tokenType: String,
)
