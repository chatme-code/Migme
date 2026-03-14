package com.projectgoth.listener;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.projectgoth.common.Logger;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * Created by houdangui on 28/4/15.
 */
public class EmoticonBmpLoadListener implements ImageHandler.ImageLoadListener {

    /**
     * by default it is true otherwise there's no chance to change to true since the
     * loading from cache is synchronised option
     */
    private boolean mIsBmpLoadedFromCache = true;

    public void setBmpLoadedFromCache(boolean isBmpLoadedCache) {
        this.mIsBmpLoadedFromCache = isBmpLoadedCache;
    }

    public EmoticonBmpLoadListener() {

    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        if (!mIsBmpLoadedFromCache) {
            //cannot create image span with the bmp loaded of emotions in TextViewEx directly, so send a event
            //to refresh the ui
            Logger.debug.log("EmoticonBmpLoadListener",  "sendBitmapFetched");
            BroadcastHandler.Emoticon.sendBitmapFetched();
        }
    }

    @Override
    public void onImageFailed(ImageView imageView) {

    }


}
