package com.projectgoth.common;

import com.migme.commonlib.enums.ImageFileType;

import android.net.Uri;

/**
 * Class for handling image uri. Provide feature for get thumbnail of the image.
 * @author ronald29x
 */
public class ImageUri {

    private Uri uri;

    /**
     * Block creating instance directly.
     */
    private ImageUri() {

    }

    /**
     * Create new instance from the provided image url.
     * @param uriString image url to be handled
     */
    public static ImageUri parse(String uriString) throws IllegalArgumentException, NullPointerException {
        ImageUri instance = new ImageUri();
        instance.uri = Uri.parse(uriString);
        if (!ImageFileType.isImageUrl(instance.uri.toString())) {
            throw new IllegalArgumentException("Not a valid image url: " + uriString);
        }

        return instance;
    }

    /**
     * Get thumbnail url.
     * @return url of the thumbnail
     */
    public String getThumbnailUrl() {
        return uri.toString();
    }
}

