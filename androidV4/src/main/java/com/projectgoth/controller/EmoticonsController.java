/**
 * Copyright (c) 2013 Project Goth
 *
 * EmoticonsController.java
 * Created Jul 12, 2013, 11:31:00 AM
 */

package com.projectgoth.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.fusion.packet.FusionPacket;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.ImageHandler.ImageLoadListener;
import com.projectgoth.listener.EmoticonBmpLoadListener;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.EmoticonJSON;
import com.projectgoth.util.LogUtils;

/**
 * Purpose: - Provides support for retrieving BaseEmoticon images from cache.
 * 
 * @author angelorohit
 * 
 */
public class EmoticonsController {

    private static EmoticonsController INSTANCE                  = null;

    private static final Object        lock                      = new Object();

    /**
     * Map containing list of images currently being requested from the server.
     * Map key is the same key that will be used to store the image in the
     * cache. Value is the timestamp at which the request was made.
     */
    private HashMap<String, Long>      mImageReqested;
    /**
     * List of emoticon hotkeys to be requested from the server. Note that the
     * request may not be done immediately. There's a delay, configurable via
     * {@link #REQUEST_EMOTICON_DELAY_MS}, which will allow to pool all the
     * requested hotkeys first before it is sent to the server.
     */
    private LinkedList<String>         mImageRequestQueue;
    /**
     * Timer delay that will be triggered when an emoticon needs to be requested
     * from the server.
     */
    private Timer                      mRequestTimer;

    private static final int           REQUEST_EMOTICON_DELAY_MS = 1000;

    /**
     * Constructor
     */
    private EmoticonsController() {
        mImageReqested = new HashMap<String, Long>();
        mImageRequestQueue = new LinkedList<String>();
        mRequestTimer = new Timer();
    }

    /**
     * A single point of entry for this controller.
     * 
     * @return An instance of the controller.
     */
    public static synchronized EmoticonsController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EmoticonsController();
        }

        return INSTANCE;
    }

    /**
     * Method to fetch Emoticon Data from fusion based on the hotkey
     * 
     * This method does not make a network call immediately. It first queues the
     * list of hotkeys to be retrieved from the server and starts a TimerTask
     * that will process the queue after {@value #REQUEST_EMOTICON_DELAY_MS}
     * seconds. The delay is intended so that requests for multiple hotkeys will
     * be fetched.
     */
    public void fetchEmoticonDataFromFusion(String key) {
        if (!TextUtils.isEmpty(key)) {
            // ignore request if we are already requesting for this key
            if (isCurrentlyRequestingForKey(key)) {
                Logger.debug.log(LogUtils.TAG_FUSION_IMAGE_FETCHER, "Request already ongoing. Ignoring request for ", key);
                return;
            }

            // only add to queue. we process it later on
            synchronized (lock) {
                Logger.debug.log("Emoticons", "Queuing image request for hotkey: ", key);
                int currSize = mImageRequestQueue.size();
                mImageRequestQueue.add(key);

                // start a delayed timer to send the request to the server
                if (currSize == 0) {
                    mRequestTimer.schedule(new EmoticonRequestTimer(), REQUEST_EMOTICON_DELAY_MS);
                }
            }
        }
    }

    /**
     * Checks if the a request for this key is currently being sent and just
     * awaiting response
     * 
     * @param hotkey
     * @return
     */
    private boolean isCurrentlyRequestingForKey(String hotkey) {
        synchronized (lock) {
            Long lastRequested = mImageReqested.get(hotkey);
            return (lastRequested != null && ((lastRequested + FusionPacket.LONG_TIMEOUT) > System.currentTimeMillis()));
        }
    }

    /**
     * Timer task that sends the actual request to the server
     * 
     * @author cherryv
     * 
     */
    private class EmoticonRequestTimer extends TimerTask {

        @Override
        public void run() {
            synchronized (lock) {
                long timeOfRequest = System.currentTimeMillis();
                Set<String> hotkeysToFetchSet = new HashSet<String>();
                for (String hotkey : mImageRequestQueue) {
                    hotkeysToFetchSet.add(hotkey);
                    mImageReqested.put(hotkey, timeOfRequest);
                }
                mImageRequestQueue.clear();
                Logger.debug.log(LogUtils.TAG_FUSION_IMAGE_FETCHER, "Making request for emoticons. Hotkey count: ",
                        hotkeysToFetchSet.size());
                EmoticonDatastore.getInstance().requestEmoticonsForHotkeys(hotkeysToFetchSet);
            }
        }
    }

    /**
     * Sets a BaseEmoticon Gift bitmap on the specified imageView. If the bitmap
     * cannot be found in cache, a request is sent to fetch the image and a
     * placeholder is set on the imageView that is passed.
     * 
     * @param imageView
     *            The view whose image is to be set.
     * @param hotkey
     *            The hotkey of the BaseEmoticon whose bitmap is to be
     *            retrieved.
     * @param defaultResImage 
     *            The default resource image id to use, when the requested image is not yet available 
     */
    public void loadGiftEmoticonImage(final ImageView imageView, final String hotkey, final int defaultResImage) {
        int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
        loadResizedBaseEmoticonImage(imageView, hotkey, size, defaultResImage, null);
    }

    /**
     * if the gift image view is a part of view of one item in a list view, it might be already recycled,
     * then we should send event to refresh the list to make sure it is shown
     *
     * */
    public void loadGiftImageInList(final ImageView imageView, final String hotkey, final int defaultResImage) {
        int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
        EmoticonBmpLoadListener listener = new EmoticonBmpLoadListener();
        Bitmap bmp = loadResizedBaseEmoticonImage(imageView, hotkey, size, defaultResImage, listener);
        listener.setBmpLoadedFromCache(bmp != null);
    }

    /**
     * Sets a BaseEmoticon Sticker bitmap on the specified imageView. If the bitmap
     * cannot be found in cache, a request is sent to fetch the image and a
     * placeholder is set on the imageView that is passed.
     * 
     * @param imageView
     *            The view whose image is to be set.
     * @param hotkey
     *            The hotkey of the BaseEmoticon whose bitmap is to be
     *            retrieved.
     * @param defaultResImage 
     *            The default resource image id to use, when the requested image is not yet available 
     */
    public void loadStickerEmoticonImage(final ImageView imageView, final String hotkey, final int defaultResImage) {
        int size = ApplicationEx.getDimension(R.dimen.sticker_request_size);
        EmoticonBmpLoadListener listener = new EmoticonBmpLoadListener();
        Bitmap bmp = loadResizedBaseEmoticonImage(imageView, hotkey, size, defaultResImage, listener);
        listener.setBmpLoadedFromCache(bmp != null);
    }
    
    /**
     * Sets a BaseEmoticon emoticon bitmap on the specified imageView. If the bitmap
     * cannot be found in cache, a request is sent to fetch the image and a
     * placeholder is set on the imageView that is passed.
     * 
     * @param imageView
     *            The view whose image is to be set.
     * @param hotkey
     *            The hotkey of the BaseEmoticon whose bitmap is to be
     *            retrieved.
     * @param defaultResImage 
     *            The default resource image id to use, when the requested image is not yet available 
     */
    public void loadEmoticonImage(final ImageView imageView, final String hotkey, final int defaultResImage) {
        int size = ApplicationEx.getDimension(R.dimen.emoticon_request_size);
        loadResizedBaseEmoticonImage(imageView, hotkey, size, defaultResImage, null);
    }
    
    /**
     * Sets a BaseEmoticon bitmap on the specified imageView. If the bitmap
     * cannot be found in cache, a request is sent to fetch the image and a
     * placeholder is set on the ImageView that is passed.
     * 
     * @param imageView
     *            The view whose image is to be set.
     * @param hotkey
     *            Unique identifier for the BaseEmoticon whose image is to be
     *            retrieved. Can be a main or alt key.
     * @param displayHeight
     *            Height of the resized image. A value less than 1 means that
     *            the Bitmap does not have to be resized.
     */
    public Bitmap loadResizedBaseEmoticonImage(final ImageView imageView, final String hotkey, final int displayHeight,
            final int defaultResImage, ImageLoadListener listener) {

        final BaseEmoticon baseEmoticon = EmoticonDatastore.getInstance().getBaseEmoticonWithHotkey(hotkey);
        Logger.debug.log("Emoticons", "loadResizedBaseEmoticonImage hotkey: ", hotkey, " displayHeight: ", displayHeight);

        Bitmap result = null;
        if (baseEmoticon != null) {
            EmoticonJSON data = baseEmoticon.getBestSize(displayHeight);
            if (data != null) {
                String url = data.getUrl();
                int width = data.getWidth();
                int height = data.getHeight();
                Logger.debug.log("Emoticons", "width: ", width, " height: ", height, " url: ", url);
                if (!TextUtils.isEmpty(url)) {
                    url = Tools.constructEmoticonUrl(baseEmoticon.getType(), url);
                    Logger.debug.log("Emoticons", "full url: ", url);

                    if (imageView != null && defaultResImage > 0) {
                        imageView.setImageResource(defaultResImage);
                    }

                    if (displayHeight > 0) {
                        result = ImageHandler.getInstance().loadImage(url, imageView, defaultResImage, width * displayHeight / height, displayHeight,
                                true, listener);
                    } else {
                        result = ImageHandler.getInstance().loadImage(url, imageView, defaultResImage, width, height,
                                true, listener);
                    }
                }
            } else {
                if (imageView != null && defaultResImage > 0) {
                    imageView.setImageResource(defaultResImage);
                }
                fetchEmoticonDataFromFusion(hotkey);
            }
        } else {
            if (imageView != null && defaultResImage > 0) {
                imageView.setImageResource(defaultResImage);
            }
            fetchEmoticonDataFromFusion(hotkey);
        }
        return result;
    }

    /**
     * Gets a resized Emoticon Bitmap.
     * 
     * @param hotkey
     *            Unique identifier of the Emoticon whose image is to be
     *            retrieved. Can be a main or alternate key.
     * @param bitmapHeight
     *            Height of the resized image. A value less that 1 means that
     *            the Bitmap does not have to be resized.
     * @return The resized Bitmap object.
     */
    public Bitmap getResizedEmoticonBitmap(final String hotkey, final int bitmapHeight) {
        return loadResizedBaseEmoticonImage(null, hotkey, bitmapHeight, -1, null);
    }

    public Bitmap getResizedEmoticonBitmap(final String hotkey, final int bitmapHeight, ImageLoadListener listener) {
        return loadResizedBaseEmoticonImage(null, hotkey, bitmapHeight, -1, listener);
    }
    
    public void setEmoticonDataReceived(String hotkey) {
        synchronized (lock) {
            mImageReqested.remove(hotkey);
        }
    }

    public void clearEmoticonsImageCache() {
        // TODO: clear persisted emoticons data 
        
        synchronized (lock) {
            mImageRequestQueue.clear();
            mImageReqested.clear();
        }
    }

}
