package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CognitoTokenResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    String expiresIn,
    @JsonProperty("token_type")
    String tokenType
){
}
