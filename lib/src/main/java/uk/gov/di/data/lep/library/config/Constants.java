package uk.gov.di.data.lep.library.config;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String YEAR_PATTERN = "yyyy";
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";
    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSS]";
}
