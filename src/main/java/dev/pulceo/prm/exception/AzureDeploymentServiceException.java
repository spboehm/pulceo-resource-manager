package dev.pulceo.prm.exception;

public class AzureDeploymentServiceException extends Exception {
    public AzureDeploymentServiceException() {
    }

    public AzureDeploymentServiceException(String message) {
        super(message);
    }

    public AzureDeploymentServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzureDeploymentServiceException(Throwable cause) {
        super(cause);
    }

    public AzureDeploymentServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
