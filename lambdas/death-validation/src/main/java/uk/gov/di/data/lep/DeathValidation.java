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
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithCorrelationID;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAuditExtensions;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.InvalidRecordFormatException;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.UUID;

public class DeathValidation
    extends LambdaHandler<GroJsonRecordWithCorrelationID>
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

            audit(event);

            mapAndPublish(event);
            return new APIGatewayProxyResponseEvent().withStatusCode(201);
        } catch (MappingException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }

    @Tracing
    private GroJsonRecordWithCorrelationID validateRequest(APIGatewayProxyRequestEvent event) {
        logger.info("Validating request");

        try {
            var headers = event.getHeaders();
            var correlationID = headers != null && headers.get("CorrelationID") != null ? headers.get("CorrelationID") : UUID.randomUUID().toString();
            var groJsonRecord = objectMapper.readValue(event.getBody(), GroJsonRecord.class);
            if (groJsonRecord.recordLockedDateTime() == null && groJsonRecord.recordUpdateDateTime() == null) {
                throw new InvalidRecordFormatException("Record has neither recordLocked and recordUpdate dateTimes");
            } else if (groJsonRecord.recordLockedDateTime() != null && groJsonRecord.recordUpdateDateTime() != null) {
                throw new InvalidRecordFormatException("Record has both recordLocked and recordUpdate dateTimes");
            }
            return new GroJsonRecordWithCorrelationID(groJsonRecord, correlationID);
        } catch (JsonProcessingException e) {
            logger.error("Failed to validate request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private void audit(GroJsonRecordWithCorrelationID event) {
        var auditDataExtensions = new DeathValidationAuditExtensions(event.correlationID());
        addAuditDataToQueue(new DeathValidationAudit(auditDataExtensions));
    }
}
