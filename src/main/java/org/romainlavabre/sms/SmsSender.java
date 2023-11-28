package org.romainlavabre.sms;

import java.util.List;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public interface SmsSender {
    String PROVIDER_SMS_MODE = "SMS_MODE";
    String PROVIDER_TWILIO   = "TWILIO";


    /**
     * @param to      Recipient
     * @param message Message
     * @return TRUE if sms sent
     */
    Result send( String to, String message, String from, String name );


    /**
     * @param to      List of recipients
     * @param message Message
     * @return An array of results by recipient
     */
    Result[] send( List< String > to, String message, String from, String name );


    class Result {
        private final String provider;

        private final String messageId;

        private final boolean success;


        protected Result( String provider, String messageId, boolean success ) {
            this.provider  = provider;
            this.messageId = messageId;
            this.success   = success;
        }


        public String getProvider() {
            return provider;
        }


        public String getMessageId() {
            return messageId;
        }


        public boolean isSuccess() {
            return success;
        }
    }
}
