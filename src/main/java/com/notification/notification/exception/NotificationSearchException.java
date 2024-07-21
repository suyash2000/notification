package com.notification.notification.exception;

public class NotificationSearchException extends RuntimeException {
    public NotificationSearchException(String message) {
        super(message);
    }

    public NotificationSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
