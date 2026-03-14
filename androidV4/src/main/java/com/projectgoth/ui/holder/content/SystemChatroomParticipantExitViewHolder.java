/**
 * Copyright (c) 2013 Project Goth
 *
 * SystemChatroomParticipantExitViewHolder.java
 * Created Dec 9, 2014, 9:55:29 AM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.SystemChatroomParticipantExitData;
import com.projectgoth.i18n.I18n;


/**
 * Represents a content view for {@link SystemChatroomParticipantExitData}.
 * @author angelorohit
 * 
 */
public class SystemChatroomParticipantExitViewHolder extends ContentViewHolder<SystemChatroomParticipantExitData, TextView> {

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link SystemChatroomParticipantExitData} to be used as data for this holder.
     */
    public SystemChatroomParticipantExitViewHolder(Context ctx, SystemChatroomParticipantExitData mimeData) {
        super(ctx, mimeData);
    }
    
    @Override
    protected void initializeView() {
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ad_chatleave_grey, 0, 0, 0);
        view.setTextColor(ApplicationEx.getColor(R.color.light_gray_text));
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.content_view_text_plain;
    }
    
    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            view.setText(String.format(I18n.tr("%s [%s] has left."), mimeData.getUsername(), mimeData.getLevel()));
            return true;
        }

        return false;
    }
}