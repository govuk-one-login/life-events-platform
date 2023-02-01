package uk.gov.gdx.datashare.uk.gov.gdx.datashare.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class PrisonerSearchApi : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 18082
  }

  fun stubLookupPrisoner() {
    stubFor(
      get(urlEqualTo("/prisoners/.*"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
              {
                "prisonerNumber": "A1234AA",
                "bookingId": "0001200924",
                "bookNumber": "38412A",
                "firstName": "Robert",
                "middleNames": "John James",
                "lastName": "Larsen",
                "dateOfBirth": "1975-04-02",
                "gender": "Female"
              }
              """.trimIndent(),
            ),
        ),

    )
  }
}
