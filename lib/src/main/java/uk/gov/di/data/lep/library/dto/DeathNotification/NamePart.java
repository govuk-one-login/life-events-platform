package uk.gov.di.data.lep.library.dto.DeathNotification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public record NamePart(
    NamePartType type,
    String value,
    LocalDate validFrom,
    LocalDate validUntil
) {

    public NamePart(NamePartType type, String value) {
        this(type, value, null, null);
    }

    public enum NamePartType {
        GIVEN_NAME("GivenName"),
        FAMILY_NAME("FamilyName");
        private final String value;
        private final static Map<String, NamePartType> CONSTANTS = new HashMap<String, NamePartType>();

        static {
            for (var c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        NamePartType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static NamePartType fromValue(String value) {
            var constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}
