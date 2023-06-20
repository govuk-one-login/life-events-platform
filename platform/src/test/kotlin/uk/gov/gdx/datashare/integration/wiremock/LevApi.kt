package uk.gov.gdx.datashare.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.junit5.WireMockExtension

const val LEV_API_PORT = 18099

fun mockLevApi(): WireMockExtension = WireMockExtension
  .newInstance()
  .options(WireMockConfiguration.wireMockConfig().port(LEV_API_PORT))
  .build()

fun stubLevApiDeath(mockLevApi: WireMockExtension) = mockLevApi.stubFor(
  WireMock.get(WireMock.urlEqualTo("/v1/registration/death/1234"))
    .willReturn(
      WireMock.aResponse()
        .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
        .withBody(
          """
                {
                  "id": 1234,
                  "date": "2008-08-08",
                  "entryNumber": null,
                  "deceased": {
                    "prefix": null,
                    "forenames": "Joan Narcissus Ouroboros",
                    "surname": "SMITH",
                    "suffix": null,
                    "maidenSurname": null,
                    "dateOfBirth": "2008-08-08",
                    "dateOfDeath": "2008-08-08",
                    "dateOfDeathQualifier": "On or about",
                    "birthplace": null,
                    "deathplace": null,
                    "ageAtDeath": null,
                    "sex": "Indeterminate",
                    "address": "888 Death House, 8 Death lane, Deadington, Deadshire",
                    "occupation": null,
                    "retired": null,
                    "causeOfDeath": null,
                    "certifiedBy": null,
                    "relationshipToPartner": null,
                    "aliases": [
                      {
                        "type": "formerly known as",
                        "forenames": "Joan Narcissus Ouroboros",
                        "surname": "WHITE"
                      },
                      {
                        "type": "otherwise",
                        "forenames": "John Narcissus Ouroboros",
                        "surname": "SMITH"
                      },
                      {},
                      {}
                    ]
                  },
                  "registrar": {
                    "signature": null,
                    "designation": null,
                    "subdistrict": "Subdistrict town",
                    "district": "District city",
                    "administrativeArea": "Adminshire"
                  },
                  "informant": {
                    "forenames": null,
                    "surname": null,
                    "address": null,
                    "qualification": null,
                    "signature": null
                  },
                  "partner": {
                    "name": null,
                    "occupation": null,
                    "retired": null
                  },
                  "mother": {
                    "name": null,
                    "occupation": null
                  },
                  "father": {
                    "name": null,
                    "occupation": null
                  },
                  "coroner": {
                    "name": null,
                    "designation": null,
                    "area": null
                  },
                  "inquestDate": null,
                  "status": {
                    "blocked": false,
                    "correction": "None",
                    "marginalNote": "None",
                    "onAuthorityOfRegistrarGeneral": null
                  }
                }
          """.trimIndent(),
        ),
    ),

)
