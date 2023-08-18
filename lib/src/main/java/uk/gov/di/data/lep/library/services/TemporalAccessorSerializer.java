package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.di.data.lep.library.config.Constants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class TemporalAccessorSerializer extends JsonSerializer<TemporalAccessor> {
    @Override
    public void serialize(TemporalAccessor value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        if (value.isSupported(ChronoField.YEAR) && value.isSupported(ChronoField.MONTH_OF_YEAR) && value.isSupported(ChronoField.DAY_OF_WEEK)){
            var localDate = LocalDate.from(value);
            jsonGenerator.writeString(localDate.format(DateTimeFormatter.ofPattern(Constants.LOCAL_DATE_PATTERN)));
        }
        else if (value.isSupported(ChronoField.YEAR) && value.isSupported(ChronoField.MONTH_OF_YEAR)){
            var yearMonth = YearMonth.from(value);
            jsonGenerator.writeString(yearMonth.format(DateTimeFormatter.ofPattern(Constants.YEAR_MONTH_PATTERN)));
        }
        else if (value.isSupported(ChronoField.YEAR)){
            var year = Year.from(value);
            jsonGenerator.writeString(year.format(DateTimeFormatter.ofPattern(Constants.YEAR_PATTERN)));
        }
        else {
            jsonGenerator.writeString(value.toString());
        }
    }
}
