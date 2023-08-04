package uk.gov.di.data.lep;

public record CognitoTokenResponse (
    String access_token,
    String expires_in,
    String token_type
){
}
