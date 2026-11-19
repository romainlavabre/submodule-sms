package org.romainlavabre.sms;

import org.romainlavabre.exception.HttpInternalServerErrorException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service( "smsSender" )
public class SmsSenderImpl implements SmsSender {

    protected final SmsSender smsSenderTwilio;
    protected final SmsMode   smsSenderSmsMode;
    protected final Smtp      smsSenderSmtp;


    public SmsSenderImpl(
            @Qualifier("smsSenderTwilio") SmsSender smsSenderTwilio,
            @Qualifier("smsSenderSmsMode") SmsMode smsSenderSmsMode,
            @Qualifier("smsSenderSmtp") Smtp smsSenderSmtp ) {
        this.smsSenderTwilio  = smsSenderTwilio;
        this.smsSenderSmsMode = smsSenderSmsMode;
        this.smsSenderSmtp    = smsSenderSmtp;
    }


    /**
     * One SMS through the provider of {@link SmsConfigurer#setProvider(String)}, SMS_MODE or SMTP, with the contract of
     * {@link SmsMode#send(String, String, String)}: failures thrown, {@link SmsConfigurer#setRedirectTo(String)} ignored.
     */
    public SmsMode.Sent send( String to, String text, String sender ) {
        switch ( getProvider() ) {
            case SmsSender.PROVIDER_SMS_MODE -> {
                return smsSenderSmsMode.send( to, text, sender );
            }
            case SmsSender.PROVIDER_SMTP -> {
                return smsSenderSmtp.send( to, text, sender );
            }
        }

        throw new HttpInternalServerErrorException( "SMS_BAD_PROVIDER" );
    }


    @Override
    public Result send( String to, String message, String from, String name ) {
        String redirectSms = SmsConfigurer.get().getRedirectTo();

        if ( redirectSms != null && !redirectSms.isBlank() && !redirectSms.toUpperCase().equals( "NONE" ) ) {
            to = redirectSms;
        }

        switch ( getProvider() ) {
            case SmsSender.PROVIDER_TWILIO -> {
                return smsSenderTwilio.send( to, message, from, name );
            }
            case SmsSender.PROVIDER_SMS_MODE -> {
                return smsSenderSmsMode.send( to, message, from, name );
            }
            case SmsSender.PROVIDER_SMTP -> {
                return smsSenderSmtp.send( to, message, from, name );
            }
        }

        throw new HttpInternalServerErrorException( "SMS_BAD_PROVIDER" );
    }


    @Override
    public Result[] send( List< String > to, String message, String from, String name ) {
        String redirectSms = SmsConfigurer.get().getRedirectTo();

        List< String > toRedirect;

        if ( redirectSms != null && !redirectSms.isBlank() && !redirectSms.toUpperCase().equals( "NONE" ) ) {
            toRedirect = new ArrayList<>();

            for ( int i = 0; i < to.size(); i++ ) {
                toRedirect.add( redirectSms );
            }
        } else {
            toRedirect = to;
        }

        switch ( getProvider() ) {
            case SmsSender.PROVIDER_TWILIO -> {
                return smsSenderTwilio.send( toRedirect, message, from, name );
            }
            case SmsSender.PROVIDER_SMS_MODE -> {
                return smsSenderSmsMode.send( toRedirect, message, from, name );
            }
            case SmsSender.PROVIDER_SMTP -> {
                return smsSenderSmtp.send( toRedirect, message, from, name );
            }
        }

        throw new HttpInternalServerErrorException( "SMS_BAD_PROVIDER" );
    }


    protected String getProvider() {
        return SmsConfigurer.get().getProvider() == null
                || SmsSender.PROVIDER_TWILIO.equals( SmsConfigurer.get().getProvider() )
                ? SmsSender.PROVIDER_TWILIO
                : SmsConfigurer.get().getProvider();
    }
}
