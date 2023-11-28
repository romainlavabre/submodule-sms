package org.romainlavabre.sms;

import org.romainlavabre.exception.HttpInternalServerErrorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service( "smsSender" )
public class SmsSenderImpl implements SmsSender {

    protected final SmsSender smsSenderTwilio;
    protected final SmsSender smsSenderSmsMode;


    public SmsSenderImpl( SmsSender smsSenderTwilio, SmsSender smsSenderSmsMode ) {
        this.smsSenderTwilio  = smsSenderTwilio;
        this.smsSenderSmsMode = smsSenderSmsMode;
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
