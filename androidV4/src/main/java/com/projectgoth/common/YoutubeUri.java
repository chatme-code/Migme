/**
 * Copyright (c) 2013 Project Goth
 * YoutubeUri.java
 * 
 * Jun 19, 2013 9:42:59 AM
 */
package com.projectgoth.common;

import android.net.Uri;

import java.util.List;
import java.util.regex.Matcher;


/**
 * @author angelorohit
 */
public class YoutubeUri {
    private Uri uri;

    /**
     * Quality for the thumbnail.
     *
     * @author ronald29x
     */
    public static enum ThumbnailQuality {
        LOW("default"),
        MEDIUM("mqdefault"),
        HIGH("hqdefault");

        private String value;

        /**
         * Construct quality with string value.
         *
         * @param value
         */
        private ThumbnailQuality(String value) {
            this.value = value;
        }

        /**
         * Get string value of the quality.
         *
         * @return value in string
         */
        public String value() {
            return this.value;
        }
    }

    /**
     * Block creating instance directly.
     */
    private YoutubeUri() {

    }

    private static String extractYoutubeUrl(String url) {
        String youtubeUrl = null;
        final Matcher m = SpannableBuilder.YOUTUBE_URL_PATTERN.matcher(url);
        if (m.find()) {
            //subString url to keep the complete host ex. we want http://youtu.be/a6XFybvTS9A
            // not youtu.be/a6XFybvTS9A
            youtubeUrl = url.substring(0, m.end());
        }
        return youtubeUrl;
    }

    /**
     * Create new instance from the provided youtube url.
     *
     * @param uriString youtube url to be handled
     */
    public static YoutubeUri parse(String uriString) throws IllegalArgumentException, NullPointerException {
        YoutubeUri instance = new YoutubeUri();
        //for check if it youtube link &
        // extracting youtube url with emotion hotkey eg. :) right after it
        String url = extractYoutubeUrl(uriString);
        if (url != null) {
            instance.uri = Uri.parse(url);
            return instance;
        } else {
            return null;
        }
    }

    public String getUrl() {
        return uri.toString();
    }


    /**
     * Get youtube video id.
     *
     * @return video id
     */
    public String getVideoId() {
        if (uri.toString().contains("youtu.be")) {
            // If this is a shortened youtube url,
            // then the videoid is in the first path segment.
            // Eg: http://youtu.be/P9zNrED3Qwg
            final List<String> pathSegments = uri.getPathSegments();
            if (pathSegments != null && !pathSegments.isEmpty()) {
                return pathSegments.get(0);
            }
        }

        //Url extracted from web is /v/ instead of watch?v=, can't get anything from getQueryParameter("v")
        if (uri.toString().contains("/v/")) {
            return uri.toString().split("/v/")[1];
        }

        if (uri.toString().contains("/embed/")) {
            return uri.toString().split("/embed/")[1];
        }

        // The videoid for a full youtube url is in the parameter named "v".
        return uri.getQueryParameter("v");
    }

    /**
     * Get thumbnail url based on the quality provided. Image format is jpeg.
     *
     * @param quality the quality of the thumbnail
     * @return url of the thumbnail
     */
    public String getThumbnailUrl(ThumbnailQuality quality) {
        return "http://img.youtube.com/vi/" + getVideoId() + "/" + quality.value() + ".jpg";
    }

    /**
     * Check if the url is a youtube url or not. Best to pass just the host instead of
     * the whole url to get better result.
     *
     * @param url the url to be checked
     * @return true if youtube url, false otherwise
     */
    public static boolean isYoutubeUrl(String url) {
        // Check that the url contains youtube or any of it's shortened
        // derivatives.
        return url.toLowerCase().contains("youtube.com") ||
                url.toLowerCase().contains("youtu.be");

    }
}

