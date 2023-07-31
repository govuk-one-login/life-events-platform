package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.services.JwtService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class AuthorisationTest {
    private static final JwtService jwtService = mock(JwtService.class);
    private static final Config config = mock(Config.class);
    private static final Authorisation underTest = new Authorisation(config, jwtService);
    private static final Context context = mock(Context.class);
    private static final DecodedJWT decodedJwt = mock(DecodedJWT.class);
    private static final Claim claim = mock(Claim.class);

    @BeforeAll
    static void setup() {
        when(context.getIdentity()).thenReturn(null);
        when(decodedJwt.getSubject()).thenReturn("testSubject");
    }

    @BeforeEach
    void refreshSetup() {
        reset(claim);
        reset(config);
        reset(jwtService);
    }

    @Test
    void authorisationAllowsAccessWithValidScope() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("scope")).thenReturn(claim);
        when(claim.asString()).thenReturn("\"EventType/TestEvent\"");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withPath("events/testEvent")
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Allow", result.policyDocument.Statement.get(0).Effect);
    }

    @Test
    void authorisationDeniesAccessWithInvalidScope() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("scope")).thenReturn(claim);
        when(claim.asString()).thenReturn("\"EventType/TestEvent\"");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withPath("events/notTestEvent")
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Deny", result.policyDocument.Statement.get(0).Effect);
    }

    @Test
    void authorisationDeniesAccessWithIncorrectScope() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("scope")).thenReturn(claim);
        when(claim.asString()).thenReturn("\"EventType/NotTestEvent\"");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withPath("events/testEvent")
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Deny", result.policyDocument.Statement.get(0).Effect);
    }

    @Test
    void authorisationDeniesAccessIfTokenHasNoScope() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("scope")).thenReturn(claim);

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withPath("events/testEvent")
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Deny", result.policyDocument.Statement.get(0).Effect);
    }

    @Test
    void authorisationThrowsAnError() {
        when(jwtService.decode(anyString())).thenThrow(new JWTVerificationException("Exception message"));

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));

        assertEquals(JWTVerificationException.class, exception.getClass());
        assertEquals("Exception message", exception.getMessage());
    }

    @Test
    void authorisationGenerateCorrectARN() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("scope")).thenReturn(claim);
        when(claim.asString()).thenReturn("\"EventType/TestEvent\"");
        when(config.getAwsRegion()).thenReturn("awsRegion");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withRequestContext(
                new ProxyRequestContext()
                    .withIdentity(new RequestIdentity())
                    .withAccountId("accountId")
                    .withApiId("apiId")
                    .withStage("stage")
                    .withHttpMethod("httpMethod")
            );

        var result = underTest.handleRequest(event, context);

        assertEquals("arn:aws:execute-api:awsRegion:accountId:apiId/stage/httpMethod/*", result.policyDocument.Statement.get(0).Resource);
    }

    @Test
    void authorisationPassesCorrectSubjectInResponseContext() {
        when(jwtService.decode(anyString())).thenReturn(decodedJwt);
        when(decodedJwt.getSubject()).thenReturn("subjectContent");
        when(decodedJwt.getClaim("scope")).thenReturn(claim);
        when(claim.asString()).thenReturn("\"EventType/TestEvent\"");

        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "authorization",
                    "Bearer accessToken"))
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        var result = underTest.handleRequest(event, context);

        assertEquals("subjectContent", result.context.get("sub"));
    }

}
