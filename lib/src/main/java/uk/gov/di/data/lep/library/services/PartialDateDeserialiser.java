package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;

public class PartialDateDeserialiser extends JsonDeserializer<TemporalAccessor> {
    @Override
    public TemporalAccessor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        var token = jsonParser.getCurrentToken();

        if (token.equals(JsonToken.VALUE_STRING)) {
            var text = jsonParser.getText().trim();
            var splitText = text.split("-");
            var countOfDashes = splitText.length - 1;
            return switch (countOfDashes) {
                case 0 -> Year.of(Integer.parseInt(text));
                case 1 -> {
                    var year = splitText[0];
                    var month = splitText[1];
                    yield YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
                }
                case 2 -> LocalDate.parse(text);
                default -> throw deserializationContext.weirdStringException(text, TemporalAccessor.class, "Not a valid temporal accessor string");
            };
        }

        throw deserializationContext.wrongTokenException(jsonParser, TemporalAccessor.class, JsonToken.VALUE_STRING, "Not a valid temporal accessor string");
    }
}
