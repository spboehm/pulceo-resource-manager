package dev.pulceo.prm.exception;

public class LinkServiceException extends Exception {

    public LinkServiceException() {
    }

    public LinkServiceException(String message) {
        super(message);
    }

    public LinkServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinkServiceException(Throwable cause) {
        super(cause);
    }

    public LinkServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
