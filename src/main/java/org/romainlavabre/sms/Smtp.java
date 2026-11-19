package org.romainlavabre.sms;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.util.MailConnectException;
import org.romainlavabre.sms.exception.SmsConnectionException;
import org.romainlavabre.sms.exception.SmsSendException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * The SMTP provider, for development only: each SMS becomes a plain text mail to a catch-all SMTP server such as
 * Mailpit, and never reaches a phone. The recipient is <code>&lt;number&gt;@sms.localhost</code>, a domain that never
 * resolves: a mail that would leave the catch-all server goes nowhere.
 */
@Service( "smsSenderSmtp" )
public class Smtp implements SmsSender {

    private static final String DOMAIN       = "sms.localhost";
    private static final int    DEFAULT_PORT = 25;


    @Override
    public Result send( String to, String message, String from, String name ) {
        try {
            return new Result( SmsSender.PROVIDER_SMTP, send( to, message, name ).messageId(), true );
        } catch ( SmsConnectionException | SmsSendException e ) {
            return new Result( SmsSender.PROVIDER_SMTP, null, false );
        }
    }


    /**
     * Same contract as {@link SmsMode#send(String, String, String)}; the parts are counted by {@link SmsSegments}.
     *
     * @param to     international number, with or without the leading +
     * @param sender the sender name, shown as the name of the sender of the mail; null for "SMS"
     * @throws SmsConnectionException the SMTP server could not be reached: nothing was sent
     * @throws SmsSendException       the SMTP server refused the mail
     */
    public SmsMode.Sent send( String to, String text, String sender ) {
        String      number = to.startsWith( "+" ) ? to.substring( 1 ) : to;
        String      name   = sender == null || sender.isBlank() ? "SMS" : sender;
        MimeMessage mail   = new MimeMessage( session() );

        try {
            mail.setFrom( new InternetAddress( "sms@" + DOMAIN, name, StandardCharsets.UTF_8.name() ) );
            mail.setRecipient( Message.RecipientType.TO, new InternetAddress( number + "@" + DOMAIN ) );
            mail.setSubject( "SMS " + name + " → +" + number, StandardCharsets.UTF_8.name() );
            mail.setText( text, StandardCharsets.UTF_8.name() );
            mail.saveChanges();

            Transport.send( mail );

            return new SmsMode.Sent( mail.getMessageID(), SmsSegments.count( text ).segments() );
        } catch ( MailConnectException e ) {
            throw new SmsConnectionException( "[sms] SMTP server unreachable", e );
        } catch ( MessagingException | UnsupportedEncodingException e ) {
            throw new SmsSendException( "[sms] the SMTP server refused the SMS", 0, e );
        }
    }


    @Override
    public Result[] send( List< String > to, String message, String from, String name ) {
        Result[] results = new Result[ to.size() ];

        int i = 0;

        for ( String recipient : to ) {
            results[ i ] = send( recipient, message, from, name );

            i++;
        }

        return results;
    }


    private static Session session() {
        URI        uri        = URI.create( SmsConfigurer.get().getSmtpUri() );
        String     timeout    = String.valueOf( SmsConfigurer.get().getTimeoutMillis() );
        Properties properties = new Properties();

        properties.put( "mail.smtp.host", uri.getHost() );
        properties.put( "mail.smtp.port", String.valueOf( uri.getPort() == -1 ? DEFAULT_PORT : uri.getPort() ) );
        properties.put( "mail.smtp.connectiontimeout", timeout );
        properties.put( "mail.smtp.timeout", timeout );
        properties.put( "mail.smtp.writetimeout", timeout );

        return Session.getInstance( properties );
    }
}
