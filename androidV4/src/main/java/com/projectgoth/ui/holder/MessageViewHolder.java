/**
 * Copyright (c) 2013 Project Goth
 *
 * MessageViewHolder.java
 * Created Jun 17, 2013, 11:34:40 AM
 */

package com.projectgoth.ui.holder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.Message;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.holder.content.ContentViewHolder.Property;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.ui.widget.ImageViewEx;

/**
 * @author angelorohit
 * 
 */
public class MessageViewHolder extends BaseMessageViewHolder {

    private final FragmentActivity                            activity;
    private final ImageView                                   avatar;
    private final ViewGroup                                   messageContainer;
    private final TextView                                    messageSender;
    private final TextView                                    timestamp;
    private final ImageView                                   messageTick;

    private boolean                                           canCollapseWithPrevMessage;
    private boolean                                           isMigPrivateChat;

    private static final String TAG = "MessageViewHolder";

    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;
    private ViewGroup                                         contentViewsContainer;

    public MessageViewHolder(FragmentActivity activity, View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache,
            boolean isMigPrivateChat) {
        super(view);
        this.activity = activity;
        this.spannableCache = spannableCache;
        this.isMigPrivateChat = isMigPrivateChat;

        avatar = (ImageView) view.findViewById(R.id.message_avatar);
        messageSender = (TextView) view.findViewById(R.id.message_sender);
        messageContainer = (ViewGroup) view.findViewById(R.id.message_container);
        timestamp = (TextView) view.findViewById(R.id.timestamp);
        messageTick = (ImageView) view.findViewById(R.id.msg_tick);
        contentViewsContainer = (ViewGroup) view.findViewById(R.id.content_views_container);
    }

    @Override
    public void setData(Message message) {
        super.setData(message);

        populateWithContentViews();
    }
    
    private void populateWithContentViews() {
        boolean didAddContentView = false;
        boolean shouldSetContainerBackground = true;

        // Remove all views from the contentViewsContainer.
        contentViewsContainer.removeAllViews();
        
        final List<MimeData> mimeDataList = data.getMimeDataList();

        for (final MimeData mimeData : mimeDataList) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(activity, mimeData);
            if (contentViewHolder != null) {
                final View contentView = contentViewHolder.getContentView();
                contentViewsContainer.addView(contentView);
                
                // The container background will be hidden if there is only one mime data in the list and 
                // the NO_MESSAGE_BACKGROUND property of the content view holder is true.
                if (shouldSetContainerBackground) {
                    shouldSetContainerBackground = 
                            !contentViewHolder.getProperty(Property.NO_MESSAGE_BACKGROUND) || mimeDataList.size() > 1;
                }

                //if standard image, do not show green background
                if (mimeData.isStandardImageType()) {
                    shouldSetContainerBackground = false;
                }

                contentView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mimeListener.onContentViewLongClick(data, mimeData);
                        return true;
                    }
                });
                
                didAddContentView = true;
            }
        }

        if (didAddContentView) {
            messageContainer.setBackgroundResource(shouldSetContainerBackground ? getBackground(isMigPrivateChat, data) : 0);
            messageContainer.setOnLongClickListener(this);
            messageContainer.setVisibility(View.VISIBLE);
            
            setAvatarAndSenderName();
            setStatusAndTimestamp(data);
        } else {
            messageContainer.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        contentViewHolder.setParameter(ContentViewHolder.Parameter.SPANNABLE_CACHE, spannableCache);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_LOADING_HEIGHT, ApplicationEx.getDimension(R.dimen.thumbnail_placeholder_height_big));
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_TYPE, ImageViewEx.ImageType.CHAT_IMAGE);
    }
    
    @Override
    protected void setParametersForContentViewAction(final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction) {
        contentViewAction.setParameter(ContentViewAction.Parameter.ACTIVITY, activity);
        contentViewAction.setParameter(ContentViewAction.Parameter.SENDER, data.getSender());
    }

    private void setStatusAndTimestamp(final Message message) {
        timestamp.setText(Tools.getMessageDisplayTime(message.getLongTimestamp()));
        
        int imageResId = getMessageTickResIdForMessage(message);
        Bitmap bitmap = imageResId != -1 ? UIUtils.getBitmapFromDrawableResource(activity, imageResId) : null;

        if (bitmap != null) {
            messageTick.setImageBitmap(bitmap);
            messageTick.setVisibility(View.VISIBLE);
        } else {
            messageTick.setVisibility(View.GONE);
        }
    }
    
    private static int getMessageTickResIdForMessage(final Message message) {         
        if (message.isOutgoing() && message.getDeliveryStatus() != null) {
            switch(message.getDeliveryStatus()) {
                case SENDING:
                    return R.drawable.msg_sending;
                case SENT_TO_SERVER:
                    return R.drawable.msg_one_tick;
                case RECEIVED_BY_RECIPIENT:
                    return R.drawable.msg_two_ticks;
                case FAILED:
                    return R.drawable.msg_send_failed;
                default:
                    // Do nothing.
                    break;
            }
        }
        
        return -1;
    }

    private void setAvatarAndSenderName() {

        messageSender.setVisibility(View.GONE);

        if(data.isPrivate()) {
            avatar.setVisibility(View.GONE);
        } else if(data.isOutgoing()) {
            avatar.setVisibility(View.GONE);
        } else if (canCollapseWithPrevMessage) { // hasSamePreviousSender
            avatar.setVisibility(View.INVISIBLE);
        } else {
            avatar.setOnClickListener(this);
            avatar.setVisibility(View.VISIBLE);
            avatar.setImageResource(R.drawable.icon_default_avatar);

            // Use the profile image of the user if it is available.
            // Otherwise simply, use the display picture guid sent in the message.
            ImageHandler.getInstance().loadDisplayPictureOrGuid(avatar, data.getSender(), Config.getInstance().getDisplayPicSizeSmall(), true, data.getDisplayPictureGuid(), R.drawable.icon_default_avatar);

            if (data.isIMChatMessage() && data.getDisplayName() != null) {
                messageSender.setText(data.getDisplayName());
            } else {
                messageSender.setText(data.getSender());
            }

            messageSender.setTextColor(getTitleTextColor(data));
            messageSender.setVisibility(View.VISIBLE);
        }
    }

    public void setCanCollapseWithPrevMessage(boolean collapseWithPrevMsg) {
        canCollapseWithPrevMessage = collapseWithPrevMsg;
    }

    private static int getBackground(final boolean isPrivateChat, final Message message) {
        if (message.isIncoming()) {
            if (message.hasOwnMention() && !isPrivateChat) {
                return R.drawable.chat_mention_background;
            } else {
                return R.drawable.chat_incoming_background;
            }
        } else if (message.hasFailed()) {
            return R.drawable.chat_outgoing_fail_background;
        }

        return R.drawable.chat_outgoing_background;
    }

    private static int getTitleTextColor(final Message msg) {
        int result = msg.getSourceColor();
        if (result == -1) {
            result = ApplicationEx.getColor(R.color.default_green);
            msg.setSourceColor(result);
        }
        return result;
    }

    @Override
    public void onMovedToScrapHeap(HostAdapterForViewHolder host) {
        int childCount = contentViewsContainer.getChildCount();
        for(int i = 0; i < childCount; ++i) {
            View v = contentViewsContainer.getChildAt(i);
            Object tag = v.getTag();
            if(tag != null && tag instanceof ContentViewHolder) {
                ContentViewHolder holder = (ContentViewHolder) tag;
                Logger.debug.flog(TAG, "onMovedToScrapHeap, holder: ", holder.getClass().getSimpleName());
            } else {
                Logger.debug.flog(TAG, "onMovedToScrapHeap, view: ", v.getClass().getSimpleName());
            }
        }
    }
}


