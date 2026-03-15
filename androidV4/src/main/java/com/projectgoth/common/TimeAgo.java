package com.projectgoth.common;

import com.projectgoth.i18n.I18n;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author warrenbalcos
 * @date Feb 27, 2012
 *
 */
public class TimeAgo {
    
    private static Calendar cal = Calendar.getInstance();
    static {
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
    }
    
    //@formatter:off
    //+ TODO: these can't be static final. Changing language will not work for these.
    private static final String[]   NAME1           = {
        I18n.tr("1 year ago"),
        I18n.tr("1 month ago"),
        I18n.tr("1 week ago"),
        I18n.tr("1 day ago"),
        I18n.tr("1 hour ago"),
        I18n.tr("1 minute ago")
    };

    private static final String[]   NAME2           = {
        I18n.tr("%s years ago"),
        I18n.tr("%s months ago"),
        I18n.tr("%s weeks ago"),
        I18n.tr("%s days ago"),
        I18n.tr("%s hours ago"),
        I18n.tr("%s minutes ago")
    };
    
    private static final int[]      CALENDAR_FIELDS = {
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DAY_OF_MONTH,
        Calendar.HOUR_OF_DAY,
        Calendar.MINUTE
    };
    //@formatter:on

    /**
     * Get the time elapsed in words based on {@link System} current time
     * 
     * @param time - timestamp
     * @return
     */
    public static String format(long time) {
        String result = I18n.tr("moments ago");     
        
        long timeDifference = Tools.getClientTimestampBasedOnServerTime() - time;
        if( timeDifference >= 0L ) {
            Calendar diff = (Calendar) cal.clone();
            diff.setTimeInMillis(timeDifference);
    
            int size = CALENDAR_FIELDS.length;
            int buff = 0;
            int txtIndex = 0;
            for (int i = 0; i < size; i++) {
                buff = diff.get(CALENDAR_FIELDS[i]) - cal.get(CALENDAR_FIELDS[i]);
                if (CALENDAR_FIELDS[i] == Calendar.DAY_OF_MONTH) {
                    // Check if days exceed 7 days and convert to a week  
                    if (buff >= 7) {
                        buff /= 7;
                    } else {
                        txtIndex++;
                    }
                }
                if (buff >= 1) {
                    result = String.format(NAME2[txtIndex], buff + "");
                    if (buff == 1) {
                        result = NAME1[txtIndex];
                    }
                    break;
                }
                txtIndex++;
            }           
        }
        else {
            // If the date of the device is wrong such that the 
            // post has gone into the future, we just display the 
            // the date when the post was made.
            final SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(Constants.FORMAT_SHORT_DATE);
            final Date messageDate = new Date(time);
            result = sdf.format(messageDate);
        }

        return result;
    }   
}
