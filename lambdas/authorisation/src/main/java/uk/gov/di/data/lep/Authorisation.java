package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.auth0.jwt.JWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.util.Collections;
import java.util.HashMap;

public class Authorisation implements RequestHandler<APIGatewayProxyRequestEvent, AuthoriserResponse> {
    protected static Logger logger = LogManager.getLogger();

    @Override
    @Tracing
    @Logging(clearState = true)
    public AuthoriserResponse handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        logger.info("Authenticating and authorising request. Event parameter: " + event.toString());
        var headers = event.getHeaders();
        var authorisationToken = headers.get("Authorization");
        var auth = "Deny";
        var sub = JWT.decode(authorisationToken).getSubject();
        if (sub != null) {
            auth = "Allow";
        }

        var eventContext = new HashMap<String, String>();
        eventContext.put("sub", sub);

        var proxyContext = event.getRequestContext();
        var identity = proxyContext.getIdentity();

        var arn = String.format("arn:aws:execute-api:%s:%s:%s/%s/%s/%s", System.getenv("AWS_REGION"), proxyContext.getAccountId(), proxyContext.getApiId(), proxyContext.getStage(), proxyContext.getHttpMethod(), "*");
        var statement = Statement.builder().effect(auth).resource(arn).build();
        var policyDocument = PolicyDocument.builder().statements(Collections.singletonList(statement)).build();

        return AuthoriserResponse.builder().principalId(identity.getAccountId()).policyDocument(policyDocument).context(eventContext).build();
    }
}
