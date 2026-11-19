package org.romainlavabre.sms;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.romainlavabre.sms.exception.SmsConnectionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SmtpTest {

    private GreenMail smtp;


    @BeforeEach
    public void start() {
        smtp = new GreenMail( ServerSetupTest.SMTP.dynamicPort() );
        smtp.start();

        configure( "SMTP", "smtp://127.0.0.1:" + smtp.getSmtp().getPort() );
    }


    @AfterEach
    public void stop() {
        smtp.stop();
    }


    @Test
    public void the_sms_becomes_a_mail_to_its_number_from_its_sender() throws Exception {
        SmsMode.Sent sent = new Smtp().send( "+33600000001", "Votre rendez-vous est demain à 9h", "PLOMBIER" );

        MimeMessage mail = smtp.getReceivedMessages()[ 0 ];

        assertThat( mail.getAllRecipients()[ 0 ].toString() ).isEqualTo( "33600000001@sms.localhost" );
        assertThat( ( ( InternetAddress ) mail.getFrom()[ 0 ] ).getPersonal() ).isEqualTo( "PLOMBIER" );
        assertThat( mail.getSubject() ).isEqualTo( "SMS PLOMBIER → +33600000001" );
        assertThat( mail.getContent().toString().strip() ).isEqualTo( "Votre rendez-vous est demain à 9h" );
        assertThat( sent ).isEqualTo( new SmsMode.Sent( mail.getMessageID(), 1 ) );
    }


    @Test
    public void the_parts_are_counted_on_the_text() {
        SmsMode.Sent sent = new Smtp().send( "33600000001", "ê".repeat( 71 ), null );

        assertThat( sent.segments() ).isEqualTo( 2 );
    }


    @Test
    public void an_unreachable_server_tells_that_nothing_left() {
        int port = smtp.getSmtp().getPort();
        smtp.stop();
        configure( "SMTP", "smtp://127.0.0.1:" + port );

        assertThatThrownBy( () -> new Smtp().send( "33600000001", "Bonjour", null ) )
                .isInstanceOf( SmsConnectionException.class );
    }


    @Test
    public void the_sender_sends_through_the_configured_provider() {
        SmsSenderImpl smsSender = new SmsSenderImpl( new Twilio(), new SmsMode(), new Smtp() );

        smsSender.send( "+33600000001", "Bonjour", "PLOMBIER" );

        assertThat( smtp.getReceivedMessages() ).hasSize( 1 );
    }


    private static void configure( String provider, String smtpUri ) {
        SmsConfigurer.init()
                .setProvider( provider )
                .setSmtpUri( smtpUri )
                .setTimeoutMillis( 2000 )
                .build();
    }
}
