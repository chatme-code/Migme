package com.projectgoth.util;

/**
 * Time utils
 * @author freddie.w
 */
public class TimeUtils {

    /**
     * Change duration timestamp to string
     * @param duration the timestamp of duration (second)
     * @return
     */
    public static String durationToString(double duration) {
        long seconds = (long) duration % 60;
        long minutes = (long) (duration / 60) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
