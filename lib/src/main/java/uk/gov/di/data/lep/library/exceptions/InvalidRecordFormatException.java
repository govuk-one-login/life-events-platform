package uk.gov.di.data.lep.library.exceptions;

public class InvalidRecordFormatException extends RuntimeException {
    public InvalidRecordFormatException(Exception e) {
        super(e);
    }
    public InvalidRecordFormatException(String message) {
        super(message);
    }
}
