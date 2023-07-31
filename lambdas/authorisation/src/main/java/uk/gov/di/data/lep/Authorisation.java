package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.AuthoriserResponse;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.services.JwtService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Authorisation implements RequestHandler<APIGatewayProxyRequestEvent, AuthoriserResponse> {
    private final static Logger logger = LogManager.getLogger();
    protected final Config config;
    protected final JwtService jwtService;
    private final Map<String, String> scopeMatchup = Map.of(
        "\"EventType/DeathNotification\"", "/events/deathNotification",
        "\"EventType/TestEvent\"", "events/testEvent"
    );

    public Authorisation() {
        this(new Config(), new JwtService());
    }

    public Authorisation(Config config, JwtService jwtService) {
        this.config = config;
        this.jwtService = jwtService;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public AuthoriserResponse handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        logger.info("Authenticating and authorising request");

        var auth = "Deny";
        var decodedJwt = getDecodedJwt(event.getHeaders());
        var scope = decodedJwt.getClaim("scope").asString();
        var allowedPath = scope != null ? scopeMatchup.get(scope) : null;

        if (Objects.equals(allowedPath, event.getPath())) {
            auth = "Allow";
        }

        var eventContext = Map.of("sub", decodedJwt.getSubject());
        var proxyContext = event.getRequestContext();
        var identity = proxyContext.getIdentity();

        var arn = String.format(
            "arn:aws:execute-api:%s:%s:%s/%s/%s/%s",
            config.getAwsRegion(),
            proxyContext.getAccountId(),
            proxyContext.getApiId(),
            proxyContext.getStage(),
            proxyContext.getHttpMethod(),
            "*"
        );
        return new AuthoriserResponse(identity.getAccountId(), auth, arn, eventContext);
    }

    private DecodedJWT getDecodedJwt(Map<String, String> headers){
        var authorisationToken = headers.get("authorization").replace("Bearer ", "");
        return jwtService.decode(authorisationToken);
    }

}
