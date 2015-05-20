package org.motechproject.nms.kilkari.exception;

/**
 * Exception that is thrown by Kilkari when the subscription cap has been met.
 */
public class SubscriptionCapException extends Exception {

    public SubscriptionCapException(String message) {
        super(message);
    }

    public SubscriptionCapException(Exception ex, String message) {
        super(message, ex);
    }
}
