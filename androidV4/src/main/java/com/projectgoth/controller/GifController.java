package com.projectgoth.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.GifDecoder;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.widget.ImageViewEx;

/**
 * Created by houdangui on 24/11/14.
 *
 * this is to control the multiple gif images which are used as loading images
 * using one thread instead of multiple threads to control multiple gif animations
 * and stop them when necessary to prevent them making the app running slow
 */

public class GifController implements Runnable {

    private static final String TAG = "GifController";
    private GifDecoder gifDecoder;
    private Thread animationThread;
    private Bitmap tmpBitmap;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean animating;
    private ArrayList<ImageView> loadingGifList = new ArrayList<ImageView>();

    private final static GifController INSTANCE = new GifController();

    private int DEFAULT_GIF_LOADING_RES = R.drawable.ad_alien_load;

    /**
     * Constructor
     */
    private GifController() {
        setGifId(DEFAULT_GIF_LOADING_RES);
    };

    /**
     * A single point of entry for this controller.
     *
     * @return An instance of the controller.
     */
    public static synchronized GifController getInstance(){
        return INSTANCE;
    }

    public void setGifId(int gifId) {
        //set byte array of the gif
        InputStream inputStream = ApplicationEx.getContext().getResources().openRawResource(gifId);
        byte[] byteArray = null;
        try {
            byteArray = Tools.getBytes(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setBytes(byteArray);
    }

    public void setBytes(final byte[] bytes) {
        gifDecoder = new GifDecoder();
        try {
            gifDecoder.read(bytes);
        } catch (final OutOfMemoryError e) {
            gifDecoder = null;
            Logger.error.log(TAG, e.getMessage(), e);
            return;
        }

    }

    public void startLoading(ImageView gif) {
        if (!loadingGifList.contains(gif)) {
            loadingGifList.add(gif);
            Logger.debug.log(TAG, "add:" + gif);
            startAnimation();
        }
    }

    public void stopLoading(ImageView gif) {
        boolean ret = loadingGifList.remove(gif);
        if (ret) {
            Logger.debug.log(TAG, "remove:" + gif);
            if (loadingGifList.isEmpty()) {
                stopAnimation();
            }
        }
    }

    public void startAnimation() {
        animating = true;

        if (animationThread == null) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }
    public void stopAnimation() {
        animating = false;

        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    public void stopAll() {
        Logger.debug.log(TAG, "stopAll");
        stopAnimation();

        if (loadingGifList != null) {
            loadingGifList.clear();
        }
    }

    @Override
    public void run() {
        Logger.debug.log(TAG, "loading thread started");

        final int n = gifDecoder.getFrameCount();
        do {
            for (int i = 0; i < n; i++) {
                if (!animating)
                    break;
                gifDecoder.advance();
                try {
                    tmpBitmap = gifDecoder.getFrame();
                    if (!animating)
                        break;
                    handler.post(updateResults);
                } catch (Exception e) {
                    Logger.warning.log(TAG, e);
                }
                if (!animating)
                    break;
                try {
                    Thread.sleep(gifDecoder.getNextDelay());
                } catch (final Exception e) {
                    // suppress any exception
                    // it can be InterruptedException or IllegalArgumentException
                }
            }

        } while (animating);

        Logger.debug.log(TAG, "thread stopped");
    }

    private final Runnable updateResults = new Runnable() {
        @Override
        public void run() {
            if (tmpBitmap != null && !tmpBitmap.isRecycled()) {
                for (ImageView imageView : loadingGifList) {
                    if (imageView instanceof ImageViewEx) {
                        ImageViewEx imageViewEx = (ImageViewEx) imageView;
                        imageViewEx.setLoadingImageBmp(tmpBitmap);
                    } else {
                        imageView.setImageBitmap(tmpBitmap);
                    }
                }
            }
        }
    };
}
