package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorisationTest {
    private static final Authorisation underTest = new Authorisation();
    private static final Context context = mock(Context.class);

    @Test
    void authorisationReturnsAnAuthorisorResponseWithCorrectActionAndEffect() {
        var event = new APIGatewayProxyRequestEvent()
            .withHeaders(
                Map.of(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
            .withRequestContext(new ProxyRequestContext().withIdentity(new RequestIdentity()));

        when(context.getIdentity()).thenReturn(null);

        var result = underTest.handleRequest(event, context);

        assertEquals("execute-api:Invoke", result.policyDocument.Statement.get(0).Action);
        assertEquals("Allow", result.policyDocument.Statement.get(0).Effect);
    }
}
