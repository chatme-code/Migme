/**
 * Copyright (c) 2013 Project Goth
 *
 * TextUtilsEx.java
 * Created Mar 7, 2014, 7:40:49 PM
 */

package com.projectgoth.util;

import com.projectgoth.common.Constants;
/**
 * Contains utilities related to Strings which are not available in
 * {@link android.text.TextUtils}.
 * 
 * @author angelorohit
 */
public abstract class StringUtils {

    /**
     * Truncates a given string to the given maximum number of characters and
     * optionally appends an ellipsis to the truncated string.
     * 
     * @param source
     *            The string to be truncated.
     * @param maxChars
     *            The max number of characters for the result string.
     * @param shouldEndWithEllipsis
     *            Indicates whether the truncated string should be ellipsized or
     *            not.
     * @return A truncated string or the original string if there was nothing to
     *         truncate.
     */
    public static String truncate(final String source, final int maxChars, final boolean shouldEndWithEllipsis) {
        if (source.length() > maxChars) {
            String result = source.substring(0, maxChars);
            if (shouldEndWithEllipsis) {
                result += Constants.ELLIPSIS;
            }
            return result;
        }

        // Nothing to truncate.
        return source;
    }

    /**
     * Encodes content to include HTML entities.
     * 
     * @param content
     *            The content to be encoded.
     */
    public static String encodeHtml(final String content) {
        String result = content;
        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replace("\"", "&quot;");
        result = result.replace("'", "&apos;");

        return result;
    }

    /**
     * Decodes content to replace HTML entities with expected characters.
     * 
     * @param content
     *            The content to be decoded.
     */
    public static String decodeHtml(final String content) {
        if (content == null)
            return null;

        String result = content;

        result = result.replace("&amp;#039;", "'");
        result = result.replace("&lt;", "<");
        result = result.replace("&gt;", ">");
        result = result.replace("&quot;", "\"");
        result = result.replace("&apos;", "'");
        result = result.replace("&amp;", "&");

        return result;
    }

    /**
     * converts content to JSON valid string
     *
     * @param content
     *            The content to be converted.
     */
    public static String convertToJsonValidString(final String content) {
        if (content == null)
            return null;

        String result = content;

        result = result.replace("\\", "\\\\");
        result = result.replace("/", "\\/");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\t", "\\t");
        result = result.replace("\"", "\\\"");

        return result;
    }
    
    public static boolean containsIgnoreCase(final String srcStr, final String compareStr) {
        return srcStr.toLowerCase().contains(compareStr.toLowerCase());
    }
    
    /**
     * Trims white spaces from the left side of a string.
     * @param str The string to be trimmed.
     * @return	The trimmed string.
     */
    public static String trimLeft(final String str) {
    	// Start from the beginning of the string and count forward until a non-white space is found.
    	int count = 0;
    	final int length = str.length();
    	while (count < length && str.charAt(count) == ' ') {
    		++count;
    	}
    	
		return str.substring(count);
    }
    
    /**
     * Trims white spaces from the right side of a string.
     * @param str	The string to be trimmed.
     * @return	The trimmed string.
     */
    public static String trimRight(final String str) {
    	// Start from the end of the string and count backward until a non-white space is found.
    	int count = str.length() - 1;
    	while (count >= 0 && str.charAt(count) == ' ') {
    		--count;
    	}
    	
		return str.substring(0, count + 1);
    }
}
