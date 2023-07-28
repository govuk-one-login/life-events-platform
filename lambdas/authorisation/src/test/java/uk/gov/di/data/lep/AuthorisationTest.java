package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.services.JwtService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorisationTest {
    private static final Config config = mock(Config.class);
    private static final JwtService jwtService = mock(JwtService.class);
    private static final Authorisation underTest = new Authorisation(config, jwtService);
    private static final Context context = mock(Context.class);
    private static final DecodedJWT decodedJwt = mock(DecodedJWT.class);

    @BeforeEach
    void refreshSetup() {
        clearInvocations(config);
        clearInvocations(jwtService);
        clearInvocations(context);
        clearInvocations(decodedJwt);
    }

    @Test
    void authorisationReturnsAnAuthorisorResponseWithCorrectActionAndEffect() {
        when(context.getIdentity()).thenReturn(null);
        when(config.getAwsRegion()).thenReturn("eu-west-2");
        when(config.getUserPoolId()).thenReturn("userPoolId");
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getSubject()).thenReturn("testSubject");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "Authorization",
                    "Bearer accessToken"))
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));


        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Allow", result.policyDocument.Statement.get(0).Effect);
    }
}
