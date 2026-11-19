# sms

Sending SMS through smsmode or Twilio.

## Segments (5.1.0)

`SmsSegments.count( text )` tells how many SMS a text costs, before sending it:

| Encoding | One SMS | Per part beyond |
|---|---|---|
| `GSM_7` (basic alphabet; `^ { } \ [ ~ ] \| €` take 2 places) | 160 | 153 |
| `UCS_2` (as soon as one character is outside GSM-7: `ê`, `ç`, `ô`, an emoji…) | 70 | 67, in UTF-16 units (an emoji counts 2) |

## `SmsMode.send( to, text, sender )` (5.1.0)

One SMS, failures thrown instead of swallowed:

```java
SmsConfigurer.init()
        .setSmsModeApiKey( key )
        .build();

SmsMode.Sent sent = smsMode.send( "+33600000001", text, "PLOMBIER" );
sent.messageId();
sent.segments(); // body.messagePartCount of smsmode, null when it does not say
```

| Outcome | Meaning |
|---|---|
| `Sent` | accepted by smsmode |
| `SmsConnectionException` | smsmode could not be reached: **nothing was sent** |
| `SmsSendException` | smsmode refused it (`getStatus()`), or its answer was lost (`0`): sending again may duplicate it |

`setSmsModeUrl` (a fake server in tests) and `setTimeoutMillis` (10 s) tune it. This method ignores
`setRedirectTo`: the caller chooses the recipient.

## `SmsSender`

`smsSender.send( to, message, from, name )` returns a `Result` (`isSuccess`, `getMessageId`) and picks the provider
from `setProvider( "SMS_MODE" | "TWILIO" )`; `setRedirectTo` sends everything to one number.

## SMTP provider (5.2.0)

For development only: each SMS becomes a plain text mail to a catch-all SMTP server (Mailpit…), never a real SMS.

```java
SmsConfigurer.init()
        .setProvider( "SMTP" )
        .setSmtpUri( "smtp://mailpit:1025" )
        .build();

SmsMode.Sent sent = smsSender.send( "+33600000001", text, "PLOMBIER" ); // SmsSenderImpl
```

The mail goes from `PLOMBIER <sms@sms.localhost>` to `33600000001@sms.localhost`. `SmsSenderImpl.send( to, text, sender )`
has the contract of `SmsMode.send( to, text, sender )` and uses the provider of `setProvider( "SMS_MODE" | "SMTP" )`;
`Sent.segments()` is counted by `SmsSegments` for SMTP.
