/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatNotificationViewHolder.java
 * Created Jun 21, 2013, 8:00:12 PM
 */

package com.projectgoth.ui.holder;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.model.Message;
import com.projectgoth.ui.holder.content.ContentViewHolder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author angelorohit
 * 
 */
public class ChatNotificationViewHolder extends BaseMessageViewHolder {
    
    private ViewGroup           contentViewsContainer;

    public ChatNotificationViewHolder(View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        super(view);
        
        contentViewsContainer = (LinearLayout) view.findViewById(R.id.content_views_container);
    }

    @Override
    public void setData(Message message) {
        super.setData(message);

        // Remove all views from the contentViewsContainer.
        contentViewsContainer.removeAllViews();
        
        boolean didAddContentView = false;
        
        final List<MimeData> mimeDataList = data.getMimeDataList();
        for (final MimeData mimeData : mimeDataList) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(ApplicationEx.getContext(), mimeData);
            if (contentViewHolder != null) {
                contentViewsContainer.addView(contentViewHolder.getContentView());
                didAddContentView = true;
            }
        }
        
        if (didAddContentView) {
            contentViewsContainer.setVisibility(View.VISIBLE);
        }
    }
}
