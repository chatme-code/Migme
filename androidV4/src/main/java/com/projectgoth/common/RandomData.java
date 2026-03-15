/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.common;

import com.projectgoth.b.data.HotTopic;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RandomData.java
 * 
 * @author warrenbalcos on Jun 4, 2013
 * 
 */
public class RandomData {


	
	private static final Random		rand				= new Random();
	
	private static final AtomicLong	longGen				= new AtomicLong();
	
	//@formatter:off
	
	private static final String[]	RANDOM_NAME			= { "Angie", "Gelo", "Dan", "Cherry",
			"Warren", "Mapet", "Elaine", "Ardy", "Marc", "Bernard", "Johan", "Ali", "Tim", "Zehua",
			"Kien", "Koko", "Sajnikanth Suriyanarayanan", "Angelo Rohit Pulikotil"			};
	
	private static final String[]	RANDOM_SENTENCES	= new String[] {
			"the cow jumped over the moon", "an apple a day keeps the doctor away",
			"four score and seven years ago", "snow white and the seven dwarfs",
			"i am at two with nature",
			"nothing so loud as hearing when we lie, the truth is not kind and you said neither am i",
			"i feel like a runaway every time you pick a fight. take a train to mexico."};
	
	private static final String[]	RANDOM_WORDS		= new String[] { "Lorem", "ipsum", "dolor",
			"sit", "amet", "consetetur", "sadipscing", "elitr", "sed", "diam", "nonumy", "eirmod",
			"tempor", "invidunt", "ut", "labore", "et", "dolore", "magna", "aliquyam", "erat",
			"sed", "diam", "voluptua", "At", "vero", "eos", "et", "accusam", "et", "justo", "duo",
			"dolores", "et", "ea", "rebum", "Stet", "clita", "kasd", "gubergren", "no", "sea",
			"takimata", "sanctus", "est"				};
	
	private static final String[]	RANDOM_CITY			= new String[] { "Singapore", "Manila",
			"Kuala Lumpur"								};
	
	private static final String[]	RANDOM_COUNTRY		= new String[] { "Singapore",
			"Republic of the Philippines", "Malaysia", "China", "Indonesia", "United States of America" };
	
    private static final String[]   RANDOM_USERNAME  = new String[] {
            "dangerbot", "darkweapon", "therealbatman", "lemon.passion", "avatarang", "angelorohit",
            "dangui.hou", "screwdisk", "hahabye", "blinkhme", "shay.peleg", "tauneutrino", "aweewee",
            "chris.sg", "phillychin", "jiggish.tenspot", "avenger9999", "jumplexar", "kaixinc",
            "sebdeckers", "vickiho"                               };
	
    private static final String[]   RANDOM_EMOTICON_HOTKEY  = new String[] {
            "(happy)","(hee)","(rofl)","(whoa)","(sad)", "(cry)","(wail)","(cheeky)","(wink)","(shock)",
            "(what)","(doh)","(blush)","(notme)","(grr)","(rage)","(cool)","(nerd)","(geek)","(hipster)",
            "(urk)","(sick)","(puke)", "(ilove)","(mwah)","(ooze)","(eh)","(right)","(bored)","(look)",
            "(blank)","(halo)","(dance)","(cuddle)","(hshape)","(star)","(shine)","(poop)","(dies)",
            "x:)",":/",":~","%)","&)",":-=",":\\",":{",":}","(migbotangry)","(migbotcool)","(migbotcry)",
            "(migbotinlove)","(migbotkiss)","(migbotouch)","(migbotsad)","(migbotshocked)","(migbothappy)",
            "(migbottongue)","(migbottaunt)","(migbotwink)"
    };
    
    private static final String[] RANDOM_GIFT_NAME = new String[] {
            "Paris Gong", "Love Mama", "Hug", "Hangers", 
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn",
            "Aquarius", "Pisces"
    };

    private static final String[]   RANDOM_GIFT_HOTKEY  = new String[] {
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)",
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)",
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)",
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)",
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)",
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", 
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)",
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)",
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)",
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)",
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", 
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)",
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)",
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", 
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)",
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)",
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)",
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", 
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)",
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)",
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)",
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", 
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)",
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)",
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)",
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)",
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)",
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", 
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", 
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", 
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)",
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", 
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", 
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)",
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)",
            "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)",
            "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)",
            "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", 
            "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", 
            "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)", "(vg_letsplay)", "(vg_timezone)", 
            "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)", "(vg_parisgong)", "(vg_lovemama)",
            "(vg_letsplay)", "(vg_timezone)", "(vg_looreyafro)", "(vg_looreyhat)", "(vg_supershotsidn)"

    };
    
    //@formatter:on

    private RandomData() {
        super();
    }

    /**
     * returns a random email.
     * 
     * @return random email
     */
    public static String getRandomEmail() {
        return getRandomUserName() + "@" + getRandomWord() + ".com";
    }

    public static String getRandomMobileNumber() {
        final String countryCode = String.valueOf(getRandomInt(10, 99));
        final String lineNumber = String.valueOf(getRandomInt(10000000, 99999999));

        return countryCode + lineNumber;
    }

    /**
     * returns a random date.
     * 
     * @return random date
     */
    public static Date getRandomDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 365 - rand.nextInt(730));
        return calendar.getTime();
    }

    public static String getRandomName() {
        return RANDOM_NAME[rand.nextInt(RANDOM_NAME.length)];
    }

    public static String getRandomEmoticonHotkey() {
        return RANDOM_EMOTICON_HOTKEY[rand.nextInt(RANDOM_EMOTICON_HOTKEY.length)];
    }

    public static String getRandomGiftHotkey() {
        return RANDOM_GIFT_HOTKEY[rand.nextInt(RANDOM_GIFT_HOTKEY.length)];
    }
    
    public static String getRandomGiftName() {
        return RANDOM_GIFT_NAME[rand.nextInt(RANDOM_GIFT_NAME.length)];
    }

    public static String getRandomGender() {
        return rand.nextBoolean() ? "male" : "female";
    }

    public static String getRandomUserName() {
        return RANDOM_NAME[rand.nextInt(RANDOM_NAME.length)] + "_" + rand.nextInt(100);
    }

    public static String getRandomRealUsername() {
        return RANDOM_USERNAME[rand.nextInt(RANDOM_USERNAME.length)];
    }

    public static String getRandomSentence() {
        return RANDOM_SENTENCES[rand.nextInt(RANDOM_SENTENCES.length)];
    }

    /**
     * returns a random sentence.
     * 
     * @param wordNumber
     *            number of word in the sentence
     * @return random sentence made of <code>wordNumber</code> words
     */
    public static String getRandomSentence(int wordNumber) {
        StringBuffer buffer = new StringBuffer(wordNumber * 12);

        int j = 0;
        while (j < wordNumber) {
            buffer.append(getRandomWord());
            buffer.append(" ");
            j++;
        }
        return buffer.toString();
    }

    public static String getRandomCity() {
        return RANDOM_CITY[rand.nextInt(RANDOM_CITY.length)];
    }

    public static String getRandomCountry() {
        return RANDOM_COUNTRY[rand.nextInt(RANDOM_COUNTRY.length)];
    }

    public static String getRandomWord() {
        return RANDOM_WORDS[rand.nextInt(RANDOM_WORDS.length)];
    }

    public static String getRandomGUID() {
        return UUID.randomUUID().toString();
    }

    public static int getRandomInt() {
        return rand.nextInt(Integer.MAX_VALUE);
    }

    public static int getRandomInt(final int lower, final int upper) {
        if (lower == 0 && upper == 0) {
            return 0;
        } else if (lower == upper) {
            return lower;
        }

        return lower + (getRandomInt() % (upper - lower + 1));
    }

    public static boolean getRandomBoolean() {
        return rand.nextBoolean();
    }

    public static long generateUniqueLongValue() {
        return longGen.addAndGet(rand.nextInt(100));
    }

    public static HotTopic getRandomHotTopic() {
        HotTopic result = new HotTopic();
        result.setName(getRandomWord());
        result.setCount(getRandomInt());
        result.setType(getRandomName());
        return result;
    }
}
