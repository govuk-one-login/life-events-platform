package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.OldFormatDeathNotification;
import uk.gov.di.data.lep.dto.OldFormatEventData;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegisteredEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationSubject;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationUpdatedEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.Name;
import uk.gov.di.data.lep.library.dto.deathnotification.NamePart;
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
import java.util.function.Predicate;

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
            deathRegistrationTime = registrationEvent.deathRegistrationTime().toLocalDateTime();
        } else if (events instanceof DeathRegistrationUpdatedEvent registrationUpdateEvent) {
            deathRegistrationTime = registrationUpdateEvent.recordUpdateTime().toLocalDateTime();
        } else {
            throw new MappingException("Failed to convert event due to unexpected event type");
        }

        var sourceIdParts = events.deathRegistration().toString().split(":");
        var sourceId = sourceIdParts[sourceIdParts.length - 1];

        var dateOfDeath = temporalAccessorToLocalDate(events.deathDate().value());
        var subject = events.subject();
        var dateOfBirth = subject.birthDate() == null || subject.birthDate().isEmpty()
            ? temporalAccessorToLocalDate(null)
            : temporalAccessorToLocalDate(subject.birthDate().get(0).value());


        var oldFormatEventData = new OldFormatEventData(
            deathRegistrationTime.toLocalDate(),
            extractNamePart(subject.name(), NamePartType.GIVEN_NAME, s -> s== null || s.isEmpty()),
            extractNamePart(subject.name(), NamePartType.FAMILY_NAME, s -> s== null || s.isEmpty()),
            subject.sex().get(0).toString(),
            dateOfDeath,
            dateOfBirth,
            null,
            null,
            extractNamePart(subject.name(), NamePartType.FAMILY_NAME, "Name before marriage"::equals),
            null,
            null,
            subject.address().get(0).postalCode()
        );

        return new OldFormatDeathNotification(
            UUID.randomUUID().toString(),
            EventType.DEATH_NOTIFICATION,
            sourceId,
            oldFormatEventData
        );
    }

    private static String extractNamePart(List<Name> names, NamePartType partType, Predicate<String> descriptionMatcher) {
        return names.stream()
            .filter(n -> descriptionMatcher.test(n.description()))
            .findFirst()
            .flatMap(o -> o.nameParts().stream()
                .filter(np -> np.type() == partType)
                .map(NamePart::value)
                .findFirst()
            ).orElse("");
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
