package org.romainlavabre.sms.exception;

/**
 * The provider refused the SMS, or the answer was lost: sending it again may duplicate it.
 */
public class SmsSendException extends RuntimeException {

    private final int status;


    public SmsSendException( String message, int status, Throwable cause ) {
        super( message, cause );
        this.status = status;
    }


    /**
     * @return the HTTP status of the provider, 0 when no answer came back
     */
    public int getStatus() {
        return status;
    }
}
