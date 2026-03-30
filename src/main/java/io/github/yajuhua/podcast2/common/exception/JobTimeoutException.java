package io.github.yajuhua.podcast2.common.exception;

public class JobTimeoutException extends RuntimeException {
    public JobTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
