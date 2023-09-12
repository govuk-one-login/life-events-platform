package uk.gov.di.data.lep.library.exceptions;

import java.net.URISyntaxException;

public class URNGenerationException extends RuntimeException {
    public URNGenerationException(URISyntaxException e) {
        super("failed to generate gro urn", e);
    }
}
