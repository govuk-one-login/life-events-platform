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
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegisteredEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationSubject;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationUpdatedEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.NamePartType;
import uk.gov.di.data.lep.library.enums.EventType;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;

public class ConvertSetToOldFormat
    extends LambdaHandler<OldFormatDeathNotification>
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
            return mapAndPublish(oldFormat);
        } catch (JsonProcessingException e) {
            logger.error("Failed to minimise request due to mapping error");
            throw new MappingException(e);
        }
    }

    private OldFormatDeathNotification convertToOldFormat(DeathNotificationSet minimisedData) {
        var events = minimisedData.events();

        LocalDateTime deathRegistrationTime;
        if (events instanceof DeathRegisteredEvent registrationEvent) {
            deathRegistrationTime = registrationEvent.deathRegistrationTime().value();
        } else if (events instanceof DeathRegistrationUpdatedEvent registrationUpdateEvent) {
            deathRegistrationTime = registrationUpdateEvent.recordUpdateTime().value();
        } else {
            throw new MappingException(new RuntimeException("Failed to convert event due to unexpected event type"));
        }

        var sourceId = events.deathRegistrationID().toString();
        var dateOfDeath = temporalAccessorToLocalDate(events.deathDate().value());
        var subject = events.subject();
        var names = getNames(subject);
        var dateOfBirth = subject.birthDate() == null || subject.birthDate().isEmpty()
            ? temporalAccessorToLocalDate(null)
            : temporalAccessorToLocalDate(subject.birthDate().get(0).value());

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

        return new OldFormatDeathNotification(
            oldFormatData,
            null,
            null
        );
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

    private LocalDate temporalAccessorToLocalDate(TemporalAccessor from) {
        if (from == null) {
            return LocalDate.of(2015, 1, 1);
        }
        if (from.isSupported(ChronoField.YEAR) && from.isSupported(ChronoField.MONTH_OF_YEAR) && from.isSupported(ChronoField.DAY_OF_WEEK)) {
            return LocalDate.from(from);
        } else if (from.isSupported(ChronoField.YEAR) && from.isSupported(ChronoField.MONTH_OF_YEAR)) {
            return LocalDate.of(from.get(ChronoField.YEAR), from.get(ChronoField.MONTH_OF_YEAR), 1);
        } else if (from.isSupported(ChronoField.YEAR)) {
            return LocalDate.of(from.get(ChronoField.YEAR), 1, 1);
        } else {
            return LocalDate.of(2015, 1, 1);
        }
    }
}
