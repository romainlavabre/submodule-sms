package org.romainlavabre.sms;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.romainlavabre.sms.exception.SmsConnectionException;
import org.romainlavabre.sms.exception.SmsSendException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SmsModeTest {

    private final AtomicReference< String > received = new AtomicReference<>();
    private final AtomicReference< String > apiKey   = new AtomicReference<>();
    private final AtomicInteger             status   = new AtomicInteger( 201 );
    private final AtomicReference< String > answer   = new AtomicReference<>();
    private       HttpServer                server;


    @BeforeEach
    public void start() throws IOException {
        server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
        server.createContext( "/sms/v1/messages", exchange -> {
            received.set( new String( exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8 ) );
            apiKey.set( exchange.getRequestHeaders().getFirst( "X-Api-Key" ) );
            byte[] body = answer.get().getBytes( StandardCharsets.UTF_8 );
            exchange.sendResponseHeaders( status.get(), body.length );

            try ( OutputStream output = exchange.getResponseBody() ) {
                output.write( body );
            }
        } );
        server.start();

        SmsConfigurer.init()
                .setSmsModeApiKey( "key" )
                .setSmsModeUrl( "http://127.0.0.1:" + server.getAddress().getPort() + "/sms/v1/messages" )
                .setTimeoutMillis( 2000 )
                .build();
    }


    @AfterEach
    public void stop() {
        server.stop( 0 );
    }


    @Test
    public void the_sms_leaves_with_its_sender_and_comes_back_with_the_parts_counted_by_smsmode() {
        answer.set( "{\"messageId\":\"m-1\",\"body\":{\"text\":\"x\",\"messagePartCount\":2}}" );

        SmsMode.Sent sent = new SmsMode().send( "+33600000001", "Bonjour", "PLOMBIER" );

        assertThat( sent ).isEqualTo( new SmsMode.Sent( "m-1", 2 ) );
        assertThat( apiKey.get() ).isEqualTo( "key" );
        assertThat( received.get() ).contains( "\"to\":\"33600000001\"" ).contains( "\"text\":\"Bonjour\"" ).contains( "\"from\":\"PLOMBIER\"" );
    }


    @Test
    public void without_a_count_from_smsmode_the_parts_stay_unknown() {
        answer.set( "{\"messageId\":\"m-2\"}" );

        assertThat( new SmsMode().send( "33600000001", "Bonjour", null ) ).isEqualTo( new SmsMode.Sent( "m-2", null ) );
        assertThat( received.get() ).doesNotContain( "from" );
    }


    @Test
    public void a_refusal_of_smsmode_is_thrown_with_its_status() {
        status.set( 400 );
        answer.set( "{\"message\":\"invalid recipient\"}" );

        assertThatThrownBy( () -> new SmsMode().send( "33600000001", "Bonjour", null ) )
                .isInstanceOf( SmsSendException.class )
                .extracting( e -> ( ( SmsSendException ) e ).getStatus() )
                .isEqualTo( 400 );
    }


    @Test
    public void an_unreachable_smsmode_tells_that_nothing_left() {
        server.stop( 0 );

        assertThatThrownBy( () -> new SmsMode().send( "33600000001", "Bonjour", null ) )
                .isInstanceOf( SmsConnectionException.class );
    }
}
