package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.FieldOptions;
import uk.gov.di.data.lep.dto.InsertDeathXmlRequest;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.DeathRegistrationGroup;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.GroPersonNameStructure;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class GroGenerateXml
    extends LambdaHandler<GroJsonRecord> {
    private final SecureRandom randomiser = new SecureRandom();
    private final XmlMapper xmlMapper;

    public GroGenerateXml() {
        this(
            new AwsService(),
            new Config(),
            Mapper.objectMapper(),
            Mapper.xmlMapper()
        );
    }

    public GroGenerateXml(AwsService awsService, Config config, ObjectMapper objectMapper, XmlMapper xmlMapper) {
        super(awsService, config, objectMapper);
        this.xmlMapper = xmlMapper;
    }

    @Tracing
    @Logging(clearState = true)
    public void handleRequest(InsertDeathXmlRequest event, Context context) {
        var numberOfRecords = event.detailType().equals("Scheduled Event") || event.numberOfRecords() == null
            ? 25
            : event.numberOfRecords();
        var fileKey = String.format("fake_gro_d_%s.xml", LocalDate.now());

        var xml = generateXml(numberOfRecords);
        awsService.putInBucket(
            config.getGroIngestionBucketName(),
            fileKey,
            xml);
        logger.info("Successfully generated GRO file {}", fileKey);
    }

    private String generateXml(int numberOfRecords) {
        var deathRecords = new DeathRegistrationGroup(
            IntStream.range(0, numberOfRecords)
                .mapToObj(i -> createDeathRecord())
                .toList(),
            numberOfRecords
        );
        try {
            return xmlMapper.writeValueAsString(deathRecords);
        } catch (JsonProcessingException e) {
            logger.error("Failed to generate GRO file", e);
            throw new MappingException(e);
        }
    }

    private GroJsonRecord createDeathRecord() {
        var genericDateTime = getRandomLocalDateTime();
        var fullDeathDate = randomiser.nextInt(10) > 2;
        var fullBirthDate = randomiser.nextInt(10) > 3;
        var aliasName = randomiser.nextInt(10) > 1;

        return new GroJsonRecord(
            randomiser.nextInt(10000000, 99999999),
            null,
            genericDateTime,
            genericDateTime,
            null,
            new GroPersonNameStructure(
                getRandomElement(FieldOptions.TITLE),
                getRandomElement(FieldOptions.FORENAME),
                getRandomElement(FieldOptions.SURNAME),
                ""
            ),
            aliasName ? getRandomElement(FieldOptions.ALIAS_NAME) : null,
            aliasName ? getRandomElement(FieldOptions.ALIAS_NAME_TYPE) : null,
            getRandomElementOrNull(FieldOptions.MAIDEN_NAME),
            getRandomElement(FieldOptions.GENDER),
            fullDeathDate ? getRandomElement(FieldOptions.DATE_OF_DEATH) : null,
            !fullDeathDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_DEATH).partialMonth() : null,
            !fullDeathDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_DEATH).partialYear() : null,
            !fullDeathDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_DEATH).freeFormatDescription() : null,
            !fullDeathDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_DEATH).qualifierText() : null,
            fullBirthDate ? getRandomElement(FieldOptions.DATE_OF_BIRTH) : null,
            !fullBirthDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_BIRTH).partialMonth() : null,
            !fullBirthDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_BIRTH).partialYear() : null,
            !fullBirthDate ? getRandomElement(FieldOptions.PARTIAL_DATE_OF_BIRTH).freeFormatDescription() : null,
            getRandomElement(FieldOptions.ADDRESS)
        );
    }

    private LocalDateTime getRandomLocalDateTime() {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(randomiser.nextLong(1692614010)), TimeZone.getDefault().toZoneId()
        );
    }

    private <T> T getRandomElement(List<T> options) {
        return options.get(randomiser.nextInt(options.size()));
    }

    private <T> T getRandomElementOrNull(List<T> options) {
        return randomiser.nextInt(10) > 5 ? getRandomElement(options) : null;
    }
}
