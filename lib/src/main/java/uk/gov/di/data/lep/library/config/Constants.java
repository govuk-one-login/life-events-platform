package uk.gov.di.data.lep.library.config;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String YEAR_PATTERN = "yyyy";
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";
    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSS]";

    public static final String OGD_DEATH_EXTRACT_DWP_NAMESPACE = "http://www.ons.gov.uk/gro/OGDDeathExtractDWP";
    public static final String PERSON_DESCRIPTIVES_NAMESPACE = "http://www.govtalk.gov.uk/people/PersonDescriptives";
    public static final String ADDRESS_AND_PERSONAL_DETAILS_NAMESPACE = "http://www.govtalk.gov.uk/people/AddressAndPersonalDetails";
    public static final String GRO_ADDRESS_DESCRIPTIVES_NAMESPACE = "http://www.ons.gov.uk/gro/people/GROAddressDescriptives";
    public static final String GRO_PERSON_DESCRIPTIVES_NAMESPACE = "http://www.ons.gov.uk/gro/people/GROPersonDescriptives";
}
