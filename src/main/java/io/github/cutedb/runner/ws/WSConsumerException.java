package io.github.cutedb.runner.ws;

/**
 * Created by barmi83 on 29/07/16.
 */
public class WSConsumerException extends RuntimeException {

    public WSConsumerException() {
        super();
    }

    public WSConsumerException(String message) {
        super(message);
    }

    public WSConsumerException(String message, Exception innerException) {
        super(message, innerException);
    }
}
