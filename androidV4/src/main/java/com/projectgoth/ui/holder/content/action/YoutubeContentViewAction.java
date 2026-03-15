/**
 * Copyright (c) 2013 Project Goth
 *
 * YoutubeContentViewAction.java
 * Created Dec 4, 2014, 10:31:57 PM
 */

package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.holder.content.YoutubeContentViewHolder;


/**
 * Represents a class that handles actions performed on the view of a {@link YoutubeContentViewHolder}.
 * @author angelorohit
 *
 */
public class YoutubeContentViewAction extends ContentViewAction<YoutubeContentViewHolder> {

    private FragmentActivity activity;
    
    public YoutubeContentViewAction(YoutubeContentViewHolder contentViewHolder) {
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
