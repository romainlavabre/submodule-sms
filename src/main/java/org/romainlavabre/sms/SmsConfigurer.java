package org.romainlavabre.sms;

import org.romainlavabre.sms.exception.NotInitializedException;

public class SmsConfigurer {
    private static SmsConfigurer INSTANCE;
    private        String        smsModeApiKey;
    private        String        twilioSid;
    private        String        twilioPrivateKey;
    private        String        provider;
    private        String        redirectTo;
    private        String        smsModeUrl    = "https://rest.smsmode.com/sms/v1/messages";
    private        int           timeoutMillis = 10_000;


    public SmsConfigurer() {
        INSTANCE = this;
    }


    protected static SmsConfigurer get() {
        if ( INSTANCE == null ) {
            throw new NotInitializedException();
        }

        return INSTANCE;
    }


    public static SmsConfigurer init() {
        return new SmsConfigurer();
    }


    protected String getProvider() {
        return provider;
    }


    /**
     * @param provider SMS_MODE | TWILIO
     * @return
     */
    public SmsConfigurer setProvider( String provider ) {
        this.provider = provider;

        return this;
    }


    protected String getSmsModeApiKey() {
        return smsModeApiKey;
    }


    /**
     * @param smsModeApiKey API provided by <a href="https://ui.smsmode.com/settings/keys">...</a>
     */
    public SmsConfigurer setSmsModeApiKey( String smsModeApiKey ) {
        this.smsModeApiKey = smsModeApiKey;

        return this;
    }


    protected String getTwilioSid() {
        return twilioSid;
    }


    /**
     * @param twilioSid SID provided by <a href="https://console.twilio.com/us1/account/keys-credentials/api-keys">...</a>
     */
    public SmsConfigurer setTwilioSid( String twilioSid ) {
        this.twilioSid = twilioSid;

        return this;
    }


    protected String getTwilioPrivateKey() {
        return twilioPrivateKey;
    }


    /**
     * @param twilioPrivateKey Private key provided by <a href="https://console.twilio.com/us1/account/keys-credentials/api-keys">...</a>
     */
    public SmsConfigurer setTwilioPrivateKey( String twilioPrivateKey ) {
        this.twilioPrivateKey = twilioPrivateKey;

        return this;
    }


    protected String getRedirectTo() {
        return redirectTo;
    }


    /**
     * @param redirectTo Phone number to redirect SMS, if null, SMS will be sent to the real number
     * @return
     */
    public SmsConfigurer setRedirectTo( String redirectTo ) {
        this.redirectTo = redirectTo;

        return this;
    }


    protected String getSmsModeUrl() {
        return smsModeUrl;
    }


    /**
     * @param smsModeUrl used by {@link SmsMode#send(String, String, String)}, default
     *                   <code>https://rest.smsmode.com/sms/v1/messages</code>
     */
    public SmsConfigurer setSmsModeUrl( String smsModeUrl ) {
        this.smsModeUrl = smsModeUrl;

        return this;
    }


    protected int getTimeoutMillis() {
        return timeoutMillis;
    }


    /**
     * @param timeoutMillis connection and answer timeout of {@link SmsMode#send(String, String, String)}, default
     *                      10 000
     */
    public SmsConfigurer setTimeoutMillis( int timeoutMillis ) {
        this.timeoutMillis = timeoutMillis;

        return this;
    }


    public void build() {
    }
}
