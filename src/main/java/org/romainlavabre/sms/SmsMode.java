package org.romainlavabre.sms;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.romainlavabre.rest.RequestBuilder;
import org.romainlavabre.rest.Response;
import org.romainlavabre.rest.Rest;
import org.romainlavabre.sms.exception.SmsConnectionException;
import org.romainlavabre.sms.exception.SmsSendException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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


    /**
     * One SMS, failures thrown instead of swallowed. Ignores {@link SmsConfigurer#setRedirectTo(String)}: the caller
     * chooses the recipient.
     *
     * @param to     international number, with or without the leading +
     * @param sender the sender name shown on the phone, 11 characters at most; null for the account default
     * @throws SmsConnectionException smsmode could not be reached: nothing was sent
     * @throws SmsSendException       smsmode refused the SMS, or its answer was lost
     */
    public Sent send( String to, String text, String sender ) {
        JsonObject body = new JsonObject();
        JsonObject recipient = new JsonObject();
        recipient.addProperty( "to", to.startsWith( "+" ) ? to.substring( 1 ) : to );
        body.add( "recipient", recipient );
        JsonObject content = new JsonObject();
        content.addProperty( "text", text );
        body.add( "body", content );

        if ( sender != null && !sender.isBlank() ) {
            body.addProperty( "from", sender );
        }

        Duration    timeout = Duration.ofMillis( SmsConfigurer.get().getTimeoutMillis() );
        HttpRequest request = HttpRequest.newBuilder( URI.create( SmsConfigurer.get().getSmsModeUrl() ) )
                .timeout( timeout )
                .header( "X-Api-Key", SmsConfigurer.get().getSmsModeApiKey() )
                .header( "Accept", "application/json" )
                .header( "Content-Type", "application/json" )
                .POST( HttpRequest.BodyPublishers.ofString( body.toString(), StandardCharsets.UTF_8 ) )
                .build();

        HttpResponse< String > response;

        try {
            response = HttpClient.newBuilder().connectTimeout( timeout ).build()
                    .send( request, HttpResponse.BodyHandlers.ofString( StandardCharsets.UTF_8 ) );
        } catch ( ConnectException | HttpConnectTimeoutException e ) {
            throw new SmsConnectionException( "[sms] smsmode unreachable", e );
        } catch ( IOException e ) {
            throw new SmsSendException( "[sms] the answer of smsmode was lost", 0, e );
        } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
            throw new SmsSendException( "[sms] interrupted while waiting for smsmode", 0, e );
        }

        if ( response.statusCode() < 200 || response.statusCode() >= 300 ) {
            throw new SmsSendException( "[sms] smsmode answered " + response.statusCode() + ": " + response.body(), response.statusCode(), null );
        }

        return Sent.parse( response.body() );
    }


    /**
     * @param segments the parts counted by smsmode (<code>body.messagePartCount</code>), null when it does not say
     */
    public record Sent( String messageId, Integer segments ) {

        static Sent parse( String json ) {
            try {
                JsonObject answer    = JsonParser.parseString( json ).getAsJsonObject();
                String     messageId = answer.has( "messageId" ) ? answer.get( "messageId" ).getAsString() : null;
                Integer    segments  = null;

                if ( answer.has( "body" ) && answer.get( "body" ).isJsonObject()
                        && answer.getAsJsonObject( "body" ).has( "messagePartCount" ) ) {
                    segments = answer.getAsJsonObject( "body" ).get( "messagePartCount" ).getAsInt();
                }

                return new Sent( messageId, segments );
            } catch ( RuntimeException e ) {
                throw new SmsSendException( "[sms] unreadable answer of smsmode: " + json, 200, e );
            }
        }
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
