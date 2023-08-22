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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class GenerateGroXml
    extends LambdaHandler<GroJsonRecord> {

    private final FieldOptions options = new FieldOptions();
    private final XmlMapper xmlMapper;
    private final Random randomiser = new Random();

    public GenerateGroXml() {
        this(
            new AwsService(),
            new Config(),
            Mapper.objectMapper(),
            Mapper.xmlMapper()
        );
    }

    public GenerateGroXml(AwsService awsService, Config config, ObjectMapper objectMapper, XmlMapper xmlMapper) {
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

        var fullDeathDate = Math.random() > 0.2;
        var fullBirthDate = Math.random() > 0.3;
        var aliasName = Math.random() > 0.1;

        return new GroJsonRecord(
            randomiser.nextInt(10000000, 99999999),
            null,
            genericDateTime,
            genericDateTime,
            null,
            new GroPersonNameStructure(
                getRandomElement(options.title),
                getRandomElement(options.forename),
                getRandomElement(options.surname),
                "",
                ""),
            aliasName ? getRandomElement(options.aliasName) : null,
            aliasName ? getRandomElement(options.aliasNameType) : null,
            getRandomElementOrNull(options.maidenName),
            getRandomElement(options.gender),
            fullDeathDate ? getRandomElement(options.dateOfDeath) : null,
            !fullDeathDate ? getRandomElement(options.partialDateOfDeath).partialMonth() : null,
            !fullDeathDate ? getRandomElement(options.partialDateOfDeath).partialYear() : null,
            !fullDeathDate ? getRandomElement(options.partialDateOfDeath).freeFormatDescription() : null,
            !fullDeathDate ? getRandomElement(options.partialDateOfDeath).qualifierText() : null,
            fullBirthDate ? getRandomElement(options.dateOfBirth) : null,
            !fullBirthDate ? getRandomElement(options.partialDateOfBirth).partialMonth() : null,
            !fullBirthDate ? getRandomElement(options.partialDateOfBirth).partialYear() : null,
            !fullBirthDate ? getRandomElement(options.partialDateOfBirth).freeFormatDescription() : null,
            getRandomElement(options.address)
        );
    }

    private LocalDateTime getRandomLocalDateTime() {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond((long) (Math.random() * 1692614010)), TimeZone.getDefault().toZoneId()
        );
    }

    private <T> T getRandomElement(List<T> options) {
        return options.get(randomiser.nextInt(options.size()));
    }

    private <T> T getRandomElementOrNull(List<T> options) {
        return Math.random() > 0.5 ? getRandomElement(options) : null;
    }
}
