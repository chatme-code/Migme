/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageContentViewAction.java
 * Created Dec 4, 2014, 11:38:32 AM
 */

package com.projectgoth.ui.holder.content.action;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.holder.content.ImageContentViewHolder;

/**
 * Represents a class that handles actions performed on the view of a {@link ImageContentViewHolder}.
 * @author angelorohit
 * 
 */
public class ImageContentViewAction extends ContentViewAction<ImageContentViewHolder> {
    
    private FragmentActivity activity;
    private String sender;

    public ImageContentViewAction(final ImageContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        if (Tools.hideVirtualKeyboard(this.activity)) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        ImageMimeData data = contentViewHolder.getMimeData();
        if(data.getBitmapByte() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data.getBitmapByte(), 0, data.getBitmapByte().length);
            ActionHandler.getInstance().displayPhotoViewerFragment(activity, null, sender, bitmap, true, false);
        } else {

            final String urlToDisplay = contentViewHolder.getMimeData().getUrl();

            if (ImageFileType.isGifUrl(urlToDisplay)) {
                ActionHandler.getInstance().displayPhotoViewerFragment(activity, urlToDisplay, sender, null, true, true);
            } else {
                ActionHandler.getInstance().displayPhotoViewerFragment(activity, urlToDisplay, sender, null, true, false);
            }
        }
    }
    
    @Override
    public void applyToView() {
        contentViewHolder.getContentView().setOnClickListener(this);
    }
    
    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);
        
        switch (parameter) {
            case ACTIVITY:
                this.activity = (FragmentActivity) value;
                break;
            case SENDER:
                this.sender = (String) value;
                break;
            default:
                break;
        }
    }
}
