package uk.gov.gdx.datashare.uk.gov.gdx.datashare.e2e

import java.net.URI

class Config {
  companion object {
    val apiUrl = System.getenv("API_URL") ?: "http://localhost:8080"

    private val cognitoTokenUrl = System.getenv("COGNITO_TOKEN_URL") ?: "http://localhost:9090/issuer1/token"
    val cognitoTokenUri: URI = URI.create(cognitoTokenUrl)
    val adminClientId = System.getenv("COGNITO_CLIENT_ID") ?: "admin"
    val adminClientSecret = System.getenv("COGNITO_CLIENT_SECRET") ?: "admin"
  }
}
