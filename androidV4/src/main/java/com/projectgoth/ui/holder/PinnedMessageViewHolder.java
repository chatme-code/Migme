/**
 * Copyright (c) 2013 Project Goth
 *
 * MessageViewHolder.java
 * Created Jun 17, 2013, 11:34:40 AM
 */

package com.projectgoth.ui.holder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.ui.widget.ImageViewEx;

/**
 * @author angelorohit
 * 
 */
public class PinnedMessageViewHolder extends BaseViewHolder<Message> {

    private final FragmentActivity                                  activity;
    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;
    private ViewGroup                                               contentViewsContainer;

    public PinnedMessageViewHolder(FragmentActivity activity, View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        super(view);
        this.activity = activity;
        this.spannableCache = spannableCache;
        contentViewsContainer = (ViewGroup) view.findViewById(R.id.pin_content_container);
        contentViewsContainer.setVisibility(View.GONE);
    }

    private static int lightenColor(int color)
    {
        int lighten = 75; // percent

        int a = (color >>> 24) & 0xff;
        int r = (color >>> 16) & 0xff;
        int g = (color >>>  8) & 0xff;
        int b = color          & 0xff;

        int newR = (255*lighten + r * (100-lighten)) / 100;
        int newG = (255*lighten + g * (100-lighten)) / 100;
        int newB = (255*lighten + b * (100-lighten)) / 100;

        return (a << 24) | (newR << 16) | (newG << 8) | newB;
    }

    @Override
    public void setData(Message message) {
        super.setData(message);

        String conversationId = message.getConversationId();
        ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (chatConversation != null && chatConversation.isChatroom()) {
//            contentViewsContainer.setBackgroundColor(lightenColor(chatConversation.getPublicChatInfo().getColor()));
            contentViewsContainer.setBackgroundColor(ApplicationEx.getColor(R.color.chat_pinnedholder_background));
        } else {
            contentViewsContainer.setBackgroundColor(ApplicationEx.getColor(R.color.chat_pinnedholder_background));
        }

        populateWithContentViews();
    }
    
    private void populateWithContentViews() {
        // Remove all views from the contentViewsContainer.
        contentViewsContainer.removeAllViews();
        
        final List<MimeData> mimeDataList = data.getMimeDataList();
        for (final MimeData mimeData : mimeDataList) {
                final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(activity, mimeData);
                if (contentViewHolder != null) {
                    final View contentView = contentViewHolder.getContentView();
                    contentViewsContainer.addView(contentView);
                }
        }
        contentViewsContainer.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        contentViewHolder.setParameter(ContentViewHolder.Parameter.SPANNABLE_CACHE, spannableCache);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_LOADING_HEIGHT, ApplicationEx.getDimension(R.dimen.thumbnail_placeholder_height_big));
        DisplayMetrics metrics = ApplicationEx.getContext().getResources().getDisplayMetrics();
        int[] paddings = {0, Math.round(8*metrics.density), 0, Math.round(8*metrics.density)};
        contentViewHolder.setParameter(ContentViewHolder.Parameter.PADDING, paddings);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_TYPE, ImageViewEx.ImageType.CHAT_PIN_IMAGE);
        int textColor = ApplicationEx.getColor(R.color.white);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.TEXT_COLOR, textColor);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IS_PINNED, new Boolean(true));
    }
    
    @Override
    protected void setParametersForContentViewAction(final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction) {
        contentViewAction.setParameter(ContentViewAction.Parameter.ACTIVITY, activity);
        contentViewAction.setParameter(ContentViewAction.Parameter.SENDER, data.getSender());
    }

    public boolean canHide() {
        return contentViewsContainer.getVisibility() == View.VISIBLE && hasContent();
    }

    public boolean canShow() {
        return contentViewsContainer.getVisibility() == View.GONE && hasContent();
    }

    public boolean hasContent() {
        return contentViewsContainer.getChildCount() != 0;
    }

    public void show() {
        contentViewsContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        contentViewsContainer.setVisibility(View.GONE);
    }
}
