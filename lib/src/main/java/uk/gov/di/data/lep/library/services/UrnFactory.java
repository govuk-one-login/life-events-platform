package uk.gov.di.data.lep.library.services;

import uk.gov.di.data.lep.library.exceptions.UrnGenerationException;

import java.net.URI;
import java.net.URISyntaxException;

public class UrnFactory {
    private UrnFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static URI generateGroDeathUrn(int recordId) {
        try {
            return new URI("urn:fdc:gro.gov.uk:2023:death:" + recordId);
        } catch (URISyntaxException e) {
            throw new UrnGenerationException(e);
        }
    }
}
