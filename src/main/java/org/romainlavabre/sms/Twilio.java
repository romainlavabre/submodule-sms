package org.romainlavabre.sms;

import org.springframework.stereotype.Service;
import java.util.List;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
@Service( "smsSenderTwilio" )
public class Twilio implements SmsSender {

    @Override
    public Result send( final String to, final String message, String from, String name ) {
        return this.core( to, message, from, name );
    }


    @Override
    public Result[] send( final List< String > to, final String message, String from, String name ) {
        final Result[] results = new Result[ to.size() ];

        int i = 0;

        for ( final String recipient : to ) {
            results[ i ] = this.core( recipient, message, from, name );

            i++;
        }

        return results;
    }


    protected Result core( String to, final String message, String from, String name ) {
        com.twilio.Twilio.init(
                SmsConfigurer.get().getTwilioSid(),
                SmsConfigurer.get().getTwilioPrivateKey()
        );

        Message result = Message
                .creator(
                        new PhoneNumber( to ),
                        new PhoneNumber( name == null ? from : name ),
                        message
                )
                .create();

        return new Result( SmsSender.PROVIDER_TWILIO, result.getSid(), result.getErrorCode() == null );
    }
}
