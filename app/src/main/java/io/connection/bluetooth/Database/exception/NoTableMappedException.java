package io.connection.bluetooth.Database.exception;

/**
 * Created by Kinjal on 10/8/2017.
 */

public class NoTableMappedException extends RuntimeException {
    public NoTableMappedException() {
        super();
    }

    public NoTableMappedException(String message) {
        super(message);
    }

    public NoTableMappedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
