package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithCorrelationId;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAuditExtensions;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.UUID;

public class DeathValidation
    extends LambdaHandler<GroJsonRecordWithCorrelationId>
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public DeathValidation() {
    }

    public DeathValidation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiRequest, Context context) {
        try {
            var event = validateRequest(apiRequest);

            var auditData = generateAuditData(event);
            addAuditDataToQueue(auditData);

            mapAndPublish(event);
            return new APIGatewayProxyResponseEvent().withStatusCode(201);
        } catch (MappingException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }

    @Tracing
    private GroJsonRecordWithCorrelationId validateRequest(APIGatewayProxyRequestEvent event) {
        logger.info("Validating request");

        try {
            var headers = event.getHeaders();
            var correlationId = headers.get("CorrelationId") != null ? headers.get("CorrelationId") : UUID.randomUUID().toString();
            var groJsonRecord = objectMapper.readValue(event.getBody(), GroJsonRecord.class);
            return new GroJsonRecordWithCorrelationId(groJsonRecord, correlationId);
        } catch (JsonProcessingException e) {
            logger.error("Failed to validate request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private DeathValidationAudit generateAuditData(GroJsonRecordWithCorrelationId event) {
        var auditDataExtensions = new DeathValidationAuditExtensions(event.correlationId());
        return new DeathValidationAudit(auditDataExtensions);
    }
}
