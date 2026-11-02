package org.romainlavabre.sms.exception;

/**
 * The provider could not be reached: nothing was sent, the SMS can be sent again.
 */
public class SmsConnectionException extends RuntimeException {

    public SmsConnectionException( String message, Throwable cause ) {
        super( message, cause );
    }
}
