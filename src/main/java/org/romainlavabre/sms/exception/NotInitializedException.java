package org.romainlavabre.sms.exception;

public class NotInitializedException extends RuntimeException {
    public NotInitializedException() {
        super( "Sms not initialized, use SmsConfigurer for fix it" );
    }
}
