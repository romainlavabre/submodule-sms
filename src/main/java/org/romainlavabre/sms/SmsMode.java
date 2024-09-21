package org.romainlavabre.sms;


import org.romainlavabre.rest.RequestBuilder;
import org.romainlavabre.rest.Response;
import org.romainlavabre.rest.Rest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service( "smsSenderSmsMode" )
public class SmsMode implements SmsSender {

    @Override
    public Result send( String to, String message, String from, String name ) {
        Response response =
                Rest.builder()
                        .post( "https://rest.smsmode.com/sms/v1/messages" )
                        .withXApiKey( SmsConfigurer.get().getSmsModeApiKey() )
                        .addHeader( "Accept", "application/json" )
                        .jsonBody( Map.of(
                                "recipient", Map.of(
                                        "to", to
                                ),
                                "body", Map.of(
                                        "text", message
                                ),
                                "from", name == null ? "" : name
                        ) )
                        .buildAndSend();


        return response.isSuccess()
                ? new Result( SmsSender.PROVIDER_SMS_MODE, response.getBodyAsMap().get( "messageId" ).toString(), true )
                : new Result( SmsSender.PROVIDER_SMS_MODE, null, false );
    }


    @Override
    public Result[] send( List< String > to, String message, String from, String name ) {
        final Result[] results = new Result[ to.size() ];

        int i = 0;

        for ( final String recipient : to ) {
            results[ i ] = send( recipient, message, from, name );

            i++;
        }

        return results;
    }
}
