/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileContentViewAction.java
 * Created Mar 4, 2015, 10:30:34 AM
 */

package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.holder.content.ProfileContentViewHolder;


/**
 * @author shiyukun
 *
 */
public class ProfileContentViewAction extends ContentViewAction<ProfileContentViewHolder>{

    private FragmentActivity activity;    
    
    public ProfileContentViewAction(ProfileContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        ActionHandler.getInstance().displayProfile(activity, contentViewHolder.getMimeData().getUsername());
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
