package uk.gov.di.data.lep.library.exceptions;

import java.net.URISyntaxException;

public class UrnGenerationException extends RuntimeException {
    public UrnGenerationException(URISyntaxException e) {
        super("failed to generate gro urn", e);
    }
}
