/**
 * Copyright (c) 2013 Project Goth
 *
 * StorageUtils.java
 * Created Jul 2, 2014, 4:11:49 PM
 */

package com.projectgoth.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;

/**
 * Contains all utilities related to file storage and manipulation.
 * 
 * @author angelorohit
 */
public class StorageUtils {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(StorageUtils.class);    

    /**
     * Saves the image data in a given {@link ImageView} to external storage.
     * 
     * @param image
     *            The {@link ImageView} whose image data is to be saved to file.
     * @param fmt
     *            The {@link Bitmap.CompressFormat} compression format to be
     *            used on the image. If this parameter is null, a default
     *            compression format of {@link Bitmap.CompressFormat#PNG} is
     *            used.
     * @param quality
     *            The compression quality to be used on the image to be saved.
     * @return true if the image data could be saved successfully and false
     *         otherwise.
     */
    public static boolean savePhotoToExternalStorage(final Bitmap imageBmp, Bitmap.CompressFormat fmt, final int quality) {
        if (imageBmp != null) {
            final String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SLASHSTR;

            try {
                // Create a file to which we will save the image data.
                OutputStream fOut = null;

                // Default to PNG format.
                if (fmt == null) {
                    fmt = Bitmap.CompressFormat.PNG;
                }

                // Image file name is formatted as
                // "migme_1402702_135313.png"
                final String timeStamp = new SimpleDateFormat(Constants.PHOTO_DATE_FORMAT).format(new Date());
                final String appName = ApplicationEx.getInstance().getResources().getString(R.string.app_name);
                final String fileExt = getFileExtensionForImageFormat(fmt);
                final String imageFilename = String.format("%s%s%s%s", appName, Constants.UNDERSCORE, timeStamp,
                        fileExt);

                File file = new File(fullPath, imageFilename);
                file.createNewFile();
                fOut = new FileOutputStream(file);

                // Compress the image to PNG format.
                imageBmp.compress(fmt, MathUtils.clamp(quality, 0, 100), fOut);
                fOut.flush();
                fOut.close();

                // Save the newly created file to external storage via
                // MediaStore.
                if (MediaStore.Images.Media.insertImage(ApplicationEx.getInstance().getContentResolver(),
                        file.getAbsolutePath(), file.getName(), file.getName()) != null) {
                    return true;
                }
            } catch (Exception e) {
                Logger.error.log(LOG_TAG, "Failed to save photo to external storage: ", e.getMessage());
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Gets the extension of an image file that uses the specified
     * {@link Bitmap.CompressFormat}.
     * 
     * @param fmt
     *            The {@link Bitmap.CompressFormat} for which the file extension
     *            is to be retrieved.
     * @return The extension of the image file prepended with a '.' character.
     *         Eg; ".png".
     */
    private static String getFileExtensionForImageFormat(Bitmap.CompressFormat fmt) {
        String fileExt = Constants.DOTSTR;
        if (fmt != null) {
            if (fmt.equals(Bitmap.CompressFormat.PNG)) {
                fileExt += ImageFileType.PNG.getExtension();
            } else if (fmt.equals(Bitmap.CompressFormat.JPEG)) {
                fileExt += ImageFileType.JPG.getExtension();
            }
        }

        return fileExt;
    }
}
