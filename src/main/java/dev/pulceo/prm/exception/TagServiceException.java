package dev.pulceo.prm.exception;

public class TagServiceException extends Exception {

    public TagServiceException() {}

    public TagServiceException(String message) {
        super(message);
    }

    public TagServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TagServiceException(Throwable cause) {
        super(cause);
    }

    public TagServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
