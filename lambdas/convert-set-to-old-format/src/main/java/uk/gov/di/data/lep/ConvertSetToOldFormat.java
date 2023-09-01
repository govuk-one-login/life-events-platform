package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.OldFormatData;
import uk.gov.di.data.lep.dto.OldFormatDataAttributes;
import uk.gov.di.data.lep.dto.OldFormatDeathNotification;
import uk.gov.di.data.lep.dto.OldFormatEventData;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationSubject;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationUpdateEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.NamePartType;
import uk.gov.di.data.lep.library.enums.EventType;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConvertSetToOldFormat
    extends LambdaHandler<String>
    implements RequestHandler<SQSEvent, String> {

    public ConvertSetToOldFormat() {
    }

    public ConvertSetToOldFormat(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }


    @Override
    @Tracing
    @Logging(clearState = true)
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            var sqsMessage = sqsEvent.getRecords().get(0);
            var minimisedData = objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class);
            var oldFormat = convertToOldFormat(minimisedData);

            return publish(oldFormat);
        } catch (JsonProcessingException e) {
            logger.error("Failed to minimise request due to mapping error");
            throw new MappingException(e);
        }
    }

    private String convertToOldFormat(DeathNotificationSet minimisedData) throws JsonProcessingException {
        var events = minimisedData.events();
        var sourceId = events.deathRegistrationID().toString();
        var dateOfDeath = (LocalDate) events.deathDate().value();
        var subject = events.subject();

        LocalDateTime deathRegistrationTime = LocalDateTime.now();
        if (events instanceof DeathRegistrationEvent registrationEvent) {
            deathRegistrationTime = registrationEvent.deathRegistrationTime().value();
        } else if (events instanceof DeathRegistrationUpdateEvent registrationUpdateEvent) {
            deathRegistrationTime = registrationUpdateEvent.recordUpdateTime().value();
        }

        var names = getNames(subject);
        var dateOfBirth = (LocalDate) subject.birthDate().get(0).value();

        var oldFormatEventData = new OldFormatEventData(
            deathRegistrationTime.toLocalDate(),
            names.get(0),
            names.get(1),
            subject.sex().get(0).toString(),
            dateOfDeath,
            dateOfBirth,
            null,
            null,
            names.get(2),
            null,
            null,
            subject.address().get(0).postalCode()
        );

        var oldFormatDataAttributes = new OldFormatDataAttributes(
            EventType.DEATH_NOTIFICATION,
            sourceId,
            oldFormatEventData,
            null
        );

        var oldFormatData = new OldFormatData(
            UUID.randomUUID().toString(),
            "events",
            oldFormatDataAttributes,
            null,
            null
        );

        var oldFormatDeathNotification = new OldFormatDeathNotification(
            oldFormatData,
            null,
            null
        );
        return objectMapper.writeValueAsString(oldFormatDeathNotification);
    }

    private static List<String> getNames(DeathRegistrationSubject subject) {
        var givenName = "";
        var familyName = "";
        var maidenName = "";

        var optionalName = subject.name().stream()
            .filter(n -> n.description() == null || n.description().isEmpty())
            .findFirst();
        if (optionalName.isPresent()) {
            var optionalGivenName = optionalName.get().nameParts().stream()
                .filter(np -> np.type() == NamePartType.GIVEN_NAME).findFirst();
            var optionalFamilyName = optionalName.get().nameParts().stream()
                .filter(np -> np.type() == NamePartType.FAMILY_NAME).findFirst();
            if (optionalGivenName.isPresent()) {
                givenName = optionalGivenName.get().value();
            }
            if (optionalFamilyName.isPresent()) {
                familyName = optionalFamilyName.get().value();
            }
        }

        var optionalMaidenName = subject.name().stream()
            .filter(n -> n.description() != null && n.description().equals("Name before marriage"))
            .findFirst();
        if (optionalMaidenName.isPresent()) {
            var optionalMaidenFamilyName = optionalMaidenName.get().nameParts().stream()
                .filter(np -> np.type() == NamePartType.FAMILY_NAME).findFirst();
            if (optionalMaidenFamilyName.isPresent()) {
                maidenName = optionalMaidenFamilyName.get().value();
            }
        }

        return List.of(givenName, familyName, maidenName);
    }
}
