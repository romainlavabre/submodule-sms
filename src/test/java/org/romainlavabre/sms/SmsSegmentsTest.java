package org.romainlavabre.sms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SmsSegmentsTest {

    @Test
    public void gsm_7_takes_160_characters_then_153_per_part() {
        assertThat( SmsSegments.count( "a".repeat( 160 ) ) ).isEqualTo( new SmsSegments.Count( SmsSegments.GSM_7, 160, 1 ) );
        assertThat( SmsSegments.count( "a".repeat( 161 ) ).segments() ).isEqualTo( 2 );
        assertThat( SmsSegments.count( "a".repeat( 306 ) ).segments() ).isEqualTo( 2 );
        assertThat( SmsSegments.count( "a".repeat( 307 ) ).segments() ).isEqualTo( 3 );
    }


    @Test
    public void the_french_letters_of_gsm_7_stay_in_gsm_7() {
        assertThat( SmsSegments.count( "Votre RDV à 14h est confirmé. Où ? Chez vous, É." ).encoding() ).isEqualTo( SmsSegments.GSM_7 );
    }


    @Test
    public void one_character_outside_gsm_7_switches_to_ucs_2_70_then_67() {
        assertThat( SmsSegments.count( "ê" + "a".repeat( 69 ) ) ).isEqualTo( new SmsSegments.Count( SmsSegments.UCS_2, 70, 1 ) );
        assertThat( SmsSegments.count( "ç" + "a".repeat( 70 ) ).segments() ).isEqualTo( 2 );
        assertThat( SmsSegments.count( "ô" + "a".repeat( 133 ) ).segments() ).isEqualTo( 2 );
        assertThat( SmsSegments.count( "ô" + "a".repeat( 134 ) ).segments() ).isEqualTo( 3 );
    }


    @Test
    public void an_extension_character_takes_two_places() {
        assertThat( SmsSegments.count( "€" + "a".repeat( 158 ) ) ).isEqualTo( new SmsSegments.Count( SmsSegments.GSM_7, 160, 1 ) );
        assertThat( SmsSegments.count( "€" + "a".repeat( 159 ) ).segments() ).isEqualTo( 2 );
    }


    @Test
    public void an_emoji_counts_two_utf_16_units() {
        assertThat( SmsSegments.count( "👍" + "a".repeat( 68 ) ) ).isEqualTo( new SmsSegments.Count( SmsSegments.UCS_2, 70, 1 ) );
        assertThat( SmsSegments.count( "👍" + "a".repeat( 69 ) ).segments() ).isEqualTo( 2 );
    }


    @Test
    public void an_empty_text_is_one_sms() {
        assertThat( SmsSegments.count( "" ).segments() ).isEqualTo( 1 );
    }
}
