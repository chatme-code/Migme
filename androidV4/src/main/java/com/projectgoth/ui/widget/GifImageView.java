/**
 * Copyright (c) 2013 Project Goth
 *
 * GifImageView.java
 * Created Sep 4, 2014, 5:54:35 PM
 */

package com.projectgoth.ui.widget;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.projectgoth.common.GifDecoder;
import com.projectgoth.common.Tools;

public class GifImageView extends ImageView implements Runnable {

    private static final String TAG = "GifDecoderView";
    private GifDecoder gifDecoder;
    private Bitmap tmpBitmap;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean animating;
    private boolean shouldClear;
    private Thread animationThread;
    private boolean cyclePlay;

    private final Runnable updateResults = new Runnable() {
        @Override
        public void run() {
            if (tmpBitmap != null && !tmpBitmap.isRecycled())
                //Logger.debug.log(TAG, "setImageBitmap");
                setImageBitmap(tmpBitmap);
        }
    };

    private final Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            if (tmpBitmap != null && !tmpBitmap.isRecycled())
                tmpBitmap.recycle();
            tmpBitmap = null;
            gifDecoder = null;
            animationThread = null;
            shouldClear = false;
        }
    };

    public GifImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GifImageView(final Context context) {
        super(context);
    }

    public void setBytes(final byte[] bytes) {
        gifDecoder = new GifDecoder();
        try {
            gifDecoder.read(bytes);
        } catch (final OutOfMemoryError e) {
            gifDecoder = null;
            Log.e(TAG, e.getMessage(), e);
            return;
        }

        if (canStart()) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void startAnimation() {
        startAnimation(cyclePlay);
    }
    
    public void startAnimation(boolean cyclePlay) {
        this.cyclePlay = cyclePlay;
        animating = true;

        if (canStart()) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    public void resetFramePointer() {
        if (gifDecoder != null) {
            gifDecoder.moveToFirstFrame();
        }
    }

    public boolean isAnimating() {
        return animating;
    }

    public void stopAnimation() {
        animating = false;

        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    public void clear() {
        animating = false;
        shouldClear = true;
        stopAnimation();
    }

    private boolean canStart() {
        return animating && gifDecoder != null && animationThread == null;
    }

    @Override
    public void run() {
        if (shouldClear) {
            handler.post(cleanupRunnable);
            return;
        }

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
                    Log.w(TAG, e);
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
            
            if (!cyclePlay) {
                //display the first frame after playing
                gifDecoder.moveToFirstFrame();
                tmpBitmap = gifDecoder.getFrame();
                handler.post(updateResults);
                stopAnimation();
            }
            
        } while (animating);
    }
    
    public boolean isCyclePlay() {
        return cyclePlay;
    }

    
    public void setCyclePlay(boolean cyclePlay) {
        this.cyclePlay = cyclePlay;
    }

    public void setGifId(int gifId) {
        //set byte array of the gif
        InputStream inputStream = getResources().openRawResource(gifId);
        byte[] byteArray = null;
        try {
            byteArray = Tools.getBytes(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setBytes(byteArray);
    }
}