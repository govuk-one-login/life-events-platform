package uk.gov.di.data.lep.exceptions;

public class GroSftpException extends RuntimeException {
    public GroSftpException(Exception e) {
        super(e);
    }

    public GroSftpException(String message) {
        super(message);
    }
}
