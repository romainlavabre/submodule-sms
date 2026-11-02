package org.romainlavabre.sms;

/**
 * How many SMS a text costs. A text with only GSM-7 characters takes 160 characters in one SMS, 153 per part
 * beyond; a single character outside GSM-7 (ê, ç, ô, an emoji…) switches the whole text to UCS-2: 70, then 67 per
 * part, counted in UTF-16 units (an emoji counts 2). The GSM-7 extension characters (^ { } \ [ ~ ] | € and the
 * form feed) take 2 places.
 */
public final class SmsSegments {

    public static final String GSM_7 = "GSM_7";
    public static final String UCS_2 = "UCS_2";

    private static final String BASIC     = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?"
            + "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà";
    private static final String EXTENSION = "^{}\\[~]|€\f";


    private SmsSegments() {
    }


    /**
     * @return at least one segment, an empty text is still one SMS
     */
    public static Count count( String text ) {
        int septets = 0;

        for ( int i = 0; i < text.length(); i++ ) {
            char character = text.charAt( i );

            if ( BASIC.indexOf( character ) >= 0 ) {
                septets++;
            } else if ( EXTENSION.indexOf( character ) >= 0 ) {
                septets += 2;
            } else {
                return new Count( UCS_2, text.length(), segments( text.length(), 70, 67 ) );
            }
        }

        return new Count( GSM_7, septets, segments( septets, 160, 153 ) );
    }


    private static int segments( int length, int single, int perPart ) {
        if ( length <= single ) {
            return 1;
        }

        return ( length + perPart - 1 ) / perPart;
    }


    /**
     * @param length GSM-7 septets or UTF-16 units
     */
    public record Count( String encoding, int length, int segments ) {
    }
}
