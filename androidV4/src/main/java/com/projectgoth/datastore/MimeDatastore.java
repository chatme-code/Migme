/**
 * Copyright (c) 2013 Project Goth
 *
 * MimeDatastore.java
 * Created Apr 9, 2015, 2:07:13 PM
 */

package com.projectgoth.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.FlickrData;
import com.projectgoth.b.data.mime.FlickrMimeData;
import com.projectgoth.b.data.mime.FlickrSizeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.data.mime.OembedData;
import com.projectgoth.b.data.mime.OembedMimeData;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetFlickrDataListener;
import com.projectgoth.nemesis.listeners.GetOembedDataListener;

/**
 * @author mapet
 *
 */
public class MimeDatastore {

    private DataCache<MimeData>        mMimeDataCache;

    private static final int           MAX_CACHE_SIZE = 20;

    private final static String        FLICKR_URL     = "https://api.flickr.com/services/rest";
    private final static String        FLICKR_SIZE    = "Medium";
    private Map<MimeType, String>      mMimeTypeRetrivedUrlMap = new HashMap<MimeType, String>();

    private MimeDatastore() {
        try {
            mMimeDataCache = new DataCache<MimeData>(MAX_CACHE_SIZE);
            initMimeTypeRetrivedUrlMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMimeTypeRetrivedUrlMap() {
        mMimeTypeRetrivedUrlMap.put(MimeType.SOUNDCLOUD, "https://soundcloud.com/oembed");
        mMimeTypeRetrivedUrlMap.put(MimeType.VIMEO, "http://vimeo.com/api/oembed.json");
    }

    private static class MimeDatastoreHolder {
        static final MimeDatastore sINSTANCE = new MimeDatastore();
    }

    public static MimeDatastore getInstance() {
        return MimeDatastoreHolder.sINSTANCE;
    }

    private void cacheMimeData(String key, MimeData result) {
        mMimeDataCache.cacheData(key, result);
    }

    public MimeData getOembedMimeData(final String url, final MimeType mimeType) {
        if (mMimeDataCache.isExpired(url)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getOembedMimeData(new GetOembedDataListener() {
                    @Override
                    public void onOembedDataReceived(OembedData data) {
                        OembedMimeData oembedMimeData = OembedMimeData.createFromUrl(url, data.getThumbnail_url(), mimeType);
                        cacheMimeData(url, oembedMimeData);
                        BroadcastHandler.Mime.sendFetchOembedReceived();
                    }
                }, mMimeTypeRetrivedUrlMap.get(mimeType), url);
            }
        }
        return mMimeDataCache.getData(url);
    }

    public MimeData getFlickrMimeData(final String url) {
        String photoId = extractFlickrPhotoIdFromUrl(url);

        if (mMimeDataCache.isExpired(url)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getFlickrImage(new GetFlickrDataListener() {

                    @Override
                    public void onFlickrDataReceived(FlickrData flickrData) {
                        createFlickrData(url, flickrData);
                    }
                }, FLICKR_URL, photoId);
            }
        }

        return mMimeDataCache.getData(url);
    }

    private void createFlickrData(final String key, final FlickrData flickrData) {
        FlickrMimeData flickrMimeData;
        FlickrSizeData[] sizeData = flickrData.getSize();

        if (sizeData != null && sizeData.length > 0) {
            for (FlickrSizeData flickrSizeData : sizeData) {
                if (flickrSizeData.getLabel().equalsIgnoreCase(FLICKR_SIZE)) {
                    flickrMimeData = FlickrMimeData.createFromUrl(flickrSizeData.getUrl(), flickrSizeData.getSource());
                    cacheMimeData(key, flickrMimeData);
                    BroadcastHandler.Mime.sendFetchFlickrReceived();
                }
            }
        }
    }

    private String extractFlickrPhotoIdFromUrl(final String url) {
        Uri uri = Uri.parse(url);
        List<String> pathSegments = uri.getPathSegments();
        return pathSegments.get(2);
    }

}