package uk.gov.di.data.lep.library.services;

import uk.gov.di.data.lep.library.exceptions.URNGenerationException;

import java.net.URI;
import java.net.URISyntaxException;

public class UrnFactory {
    private UrnFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static URI generateGroURN(int year, int recordId) {
        try {
            return new URI("urn:fdc:gro.gov.uk:" + year + ":" + recordId);
        } catch (URISyntaxException e) {
            throw new URNGenerationException(e);
        }
    }
}
