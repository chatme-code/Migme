
package com.projectgoth.util;

import com.projectgoth.common.LevelLogger;

public abstract class AndroidLogger extends LevelLogger {

    private static final int MAX_LOG_TAG_LENGTH = 23;
    
    /**
     * Ensures that the log tag doesn't exceed its length limit
     * @param str the log tag
     * @return
     */
    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH) {
            return str.substring(0, MAX_LOG_TAG_LENGTH - 1);
        }
        return str;
    }

    /**
     * WARNING: Don't use this when obfuscating class names with Proguard!
     */
    public static String makeLogTag(Class<?> cls) {
        return makeLogTag(cls.getSimpleName());
    }

}
