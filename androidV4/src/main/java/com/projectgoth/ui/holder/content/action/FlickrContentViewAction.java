/**
 * Copyright (c) 2013 Project Goth
 *
 * FlickrContentViewAction.java
 * Created Feb 10, 2015, 2:43:31 PM
 */

package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.holder.content.FlickrContentViewHolder;

/**
 * @author mapet
 * 
 */
public class FlickrContentViewAction extends ContentViewAction<FlickrContentViewHolder> {
    
    private FragmentActivity activity;

    public FlickrContentViewAction(FlickrContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        UrlHandler.displayUrl(activity, contentViewHolder.getMimeData().getUrl());
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
            default:
                break;
        }
    }

}
