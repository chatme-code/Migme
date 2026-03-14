/**
 * Copyright (c) 2013 Project Goth
 *
 * FileUtils.java
 * Created Aug 1, 2014, 4:46:12 PM
 */

package com.projectgoth.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.projectgoth.common.Logger;

/**
 * @author warrenbalcos
 * 
 */
public class FileUtils {

    public static final String TAG                 = "FileUtils";

    public static final int    DEFAULT_FILE_BUFFER = 1024;

    public static boolean doesFileExist(Context context, String filename) {
        boolean result = false;

        InputStream is = null;
        try {
            is = context.openFileInput(filename);
            result = true;
        } catch (FileNotFoundException e) {
            // Nothing to do
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Nothing to do
                }
                is = null;
            }
        }

        return result;
    }

    /**
     * Loads specific asset file data
     */
    public static InputStream loadAssetFile(Context context, String filename) {
        AssetManager assetMgr = context.getAssets();
        try {
            return assetMgr.open(filename);
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed loading asset file: " + filename, e);
        }
        return null;
    }

    /**
     * 
     * 
     * @param context
     * @param filename
     * @return
     */
    public static InputStream loadFile(Context context, String filename) {
        try {
            return context.openFileInput(filename);
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed loading file: " + filename, e);
        }
        return null;
    }

    /**
     * Copy source file to destination
     * 
     * @param src
     * @param dst
     * @return
     */
    public static boolean saveToFile(Context context, InputStream src, String dst) {
        boolean result = false;

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(dst, Context.MODE_PRIVATE);
            int read = 0;
            byte[] bytes = new byte[DEFAULT_FILE_BUFFER];
            while ((read = src.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }
            result = true;
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed saving to file: " + dst, e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    // Nothing to do
                }
                fos = null;
            }

            if (src != null) {
                try {
                    src.close();
                } catch (Exception e) {
                    // Nothing to do
                }
                src = null;
            }
        }
        return result;
    }

}
