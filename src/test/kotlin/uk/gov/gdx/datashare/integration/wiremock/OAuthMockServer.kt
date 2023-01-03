package uk.gov.gdx.datashare.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class OAuthMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 19090
  }

  fun stubGrantToken() {
    stubFor(
      post(urlEqualTo("/token"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """{
                    "token_type": "bearer",
                    "access_token": "ABCDE"
                }
              """.trimIndent()
            )
        )
    )
  }

  fun stubOpenId() {
    stubFor(
      get(urlEqualTo("/.well-known/openid-configuration"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                                {
                                  "issuer": "http://localhost:19090",
                                  "authorization_endpoint": "http://localhost:19090/authorize",
                                  "end_session_endpoint": "http://localhost:19090/endsession",
                                  "token_endpoint": "http://localhost:19090/token",
                                  "userinfo_endpoint": "http://localhost:19090/userinfo",
                                  "jwks_uri": "http://localhost:19090/jwks",
                                  "introspection_endpoint": "http://localhost:19090/introspect",
                                  "response_types_supported": [
                                    "query",
                                    "fragment",
                                    "form_post"
                                  ],
                                  "subject_types_supported": [
                                    "public"
                                  ],
                                  "id_token_signing_alg_values_supported": [
                                    "ES256",
                                    "ES384",
                                    "RS256",
                                    "RS384",
                                    "RS512",
                                    "PS256",
                                    "PS384",
                                    "PS512"
                                  ]
                                }                                
              """.trimIndent()
            )
        )

    )
  }
}
