package dev.pulceo.prm.exception;

public class ProviderServiceException extends Exception {

    public ProviderServiceException() {
    }

    public ProviderServiceException(String message) {
        super(message);
    }

    public ProviderServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderServiceException(Throwable cause) {
        super(cause);
    }

    public ProviderServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
