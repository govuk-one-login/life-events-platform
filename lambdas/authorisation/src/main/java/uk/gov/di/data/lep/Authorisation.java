package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.AuthoriserResponse;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.services.JwtService;

import java.util.HashMap;

public class Authorisation implements RequestHandler<APIGatewayProxyRequestEvent, AuthoriserResponse> {
    private final static Logger logger = LogManager.getLogger();
    protected final Config config;
    protected final JwtService jwtService;

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
        // This is placeholder logic
        logger.info("Authenticating and authorising request");
        var headers = event.getHeaders();
        var authorisationToken = headers.get("Authorization").replace("Bearer ", "");
        var auth = "Deny";
        var decodedJwt = jwtService.decode(authorisationToken);
        var sub = decodedJwt.getSubject();
        if (sub != null) {
            auth = "Allow";
        }

        var eventContext = new HashMap<String, String>();
        eventContext.put("sub", sub);

        var proxyContext = event.getRequestContext();
        var identity = proxyContext.getIdentity();

        var arn = String.format(
            "arn:aws:execute-api:%s:%s:%s/%s/%s/%s",
            System.getenv("AWS_REGION"),
            proxyContext.getAccountId(),
            proxyContext.getApiId(),
            proxyContext.getStage(),
            proxyContext.getHttpMethod(),
            "*"
        );
        return new AuthoriserResponse(identity.getAccountId(), auth, arn, eventContext);
    }
}
