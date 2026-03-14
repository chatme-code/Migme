/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageContentViewHolder.java
 * Created Dec 2, 2014, 11:33:38 AM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.YoutubeUri;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.ImageViewEx.IconOverlay;
import com.projectgoth.util.CrashlyticsLog;


/**
 * Represents a content view holder for image content.
 * @author angelorohit
 */
public abstract class BaseImageContentViewHolder<T extends ImageMimeData> extends ContentViewHolder<T, ImageViewEx>{

    private boolean displayThumbnail = true;
    private IconOverlay mIconOverlay;

    /**
     * Constructor.
     * @param ctx           The {@link Context} to be used for inflation. 
     * @param mimeData      The {@link ImageMimeData} to be used as data for this holder.
     * @param iconOverlay   The {@link IconOverlay} to be used as the overlay for the {@link ImageViewEx}.
     */
    protected BaseImageContentViewHolder(final Context ctx, final T mimeData, final IconOverlay iconOverlay) {
        super(ctx, mimeData);
        mIconOverlay = iconOverlay;
    }
    
    /**
     * Sets the icon overlay for the {@link ImageViewEx} which is the content view of this holder.
     * @param iconOverlay The {@link IconOverlay} to be set.
     */
    protected void setIconOverlay(final IconOverlay iconOverlay) {
        mIconOverlay = iconOverlay;
        view.setIconOverlay(iconOverlay);
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.content_view_image;
    }
    
    @Override
    protected void initializeView() {
        view.setBorder(false);
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            if (mimeData.getBitmapByte() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(mimeData.getBitmapByte(), 0, mimeData.getBitmapByte().length);
                view.setImageBitmap(bitmap);
            } else {
                int placeHolderResId = R.drawable.ad_gallery_grey;
                String url = mimeData.getUrl();

                if (shouldEnsureYoutubeThumbnail()) {
                    if (TextUtils.isEmpty(mimeData.getThumbnailUrl()) && !TextUtils.isEmpty(url)) {
                        YoutubeUri uri = YoutubeUri.parse(url);
                        if (uri != null) {
                            mimeData.setThumbnailUrl(uri.getThumbnailUrl(Constants.DEFAULT_YOUTUBE_QUALITY));
                        } else {
                            CrashlyticsLog.log(new NullPointerException(), "Not a youtube url.");
                        }
                    }
                }

                if (displayThumbnail || isPlayableThumbnail()) {
                    url = mimeData.getThumbnailUrl();
                }
                view.setTag(R.id.overlay_icon, mIconOverlay);
                ImageHandler.getInstance().loadImage(url, view, placeHolderResId);
            }
            return true;
        }
        
        return false;
    }

    protected boolean isPlayableThumbnail() {
        return false;
    }

    protected boolean shouldEnsureYoutubeThumbnail() {
        return false;
    }
    
    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {
            case DISPLAY_THUMBNAIL:
                this.displayThumbnail = (Boolean) value;
                break;
            case IMAGE_LOADING_HEIGHT:
                view.setPlaceHolderHeight((Integer) value);
                break;
            case IMAGE_TYPE:
                view.setImageTyep((ImageViewEx.ImageType) value);
                break;
            default:
                break;
        }
    }

}
