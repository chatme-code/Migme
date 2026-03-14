/**
 * Copyright (c) 2013 Project Goth
 *
 * MessageListAdapter.java
 * Created Jun 17, 2013, 11:34:13 AM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.listener.MimeContentViewListener;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.ui.holder.BaseMessageViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ChatBannerViewHolder;
import com.projectgoth.ui.holder.ChatNotificationViewHolder;
import com.projectgoth.ui.holder.GiftMessageViewHolder;
import com.projectgoth.ui.holder.MessageViewHolder;

/**
 * @author mapet
 * 
 */
public class MessageListAdapter extends BaseAdapter implements AbsListView.RecyclerListener, BaseViewHolder.HostAdapterForViewHolder {

    private FragmentActivity                                  activity;
    private List<Message>                                     messagesList                 = new ArrayList<Message>();
    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache               = new ConcurrentHashMap<String, SpannableStringBuilder>();
    private LayoutInflater                                    mInflater;
    private BaseViewListener<Message>                         listener;
    private MimeContentViewListener                           mimeListener;

    private String                                            conversationId;
    private boolean                                           isMigPrivateChat;

    private String                                            latestViewedMsgId            = null;
    private String                                            latestViewedMsgIdLastTime    = null;
    private int                                               latestViewedMsgIndex         = -1;
    private int                                               latestViewedMsgIndexLastTime = -1;

    public MessageListAdapter(FragmentActivity activity) {
        super();
        
        this.activity = activity; 
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public @NonNull Message getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getViewTypeCount() {
        return Message.Type.values().length;
    }

    public final Message.Type getItemViewTypeEnum(int position) {
        Message msg = getItem(position);
        return msg.getType();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewTypeEnum(position).ordinal();
    }

    private View setupMessageViewHolder(View convertView, ViewGroup parent, int position, int layoutId) {
        MessageViewHolder messageViewHolder;
        if (convertView != null) {
            messageViewHolder = (MessageViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(layoutId, parent, false);
            messageViewHolder = new MessageViewHolder(activity, convertView, spannableCache, isMigPrivateChat);
            convertView.setTag(messageViewHolder);
        }

        messageViewHolder.setCanCollapseWithPrevMessage(canCollapseWithPrevMessage(position));
        setupBaseMessageViewHolder(messageViewHolder, position);

        return convertView;
    }

    private View setupChatNotificationViewHolder(View convertView, ViewGroup parent, int position, int layoutId) {
        
        ChatNotificationViewHolder notificationViewHolder;
        if (convertView != null) {
            notificationViewHolder = (ChatNotificationViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(layoutId, parent, false);
            notificationViewHolder = new ChatNotificationViewHolder(convertView, spannableCache);
            convertView.setTag(notificationViewHolder);
        }

        setupBaseMessageViewHolder(notificationViewHolder, position);

        return convertView;
    }

    private View setupGiftMessageViewHolder(View convertView, ViewGroup parent, int position, int layoutId) {

        GiftMessageViewHolder giftMsgViewHolder;
        if (convertView != null) {
            giftMsgViewHolder = (GiftMessageViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(layoutId, parent, false);
            giftMsgViewHolder = new GiftMessageViewHolder(convertView);
            convertView.setTag(giftMsgViewHolder);
        }

        setupBaseMessageViewHolder(giftMsgViewHolder, position);

        return convertView;
    }
    
    private void setupBaseMessageViewHolder(BaseMessageViewHolder holder, int position) {
        Message msg = getItem(position);
        holder.setDisplayDateTitleAndNewMessageIndicator(displayDateTitleWithMessage(position), displayUnreadMessageSeparator(position));
        holder.setData(msg);
        holder.setBaseViewListener(listener);
        holder.setMimeItemLongClickListener(mimeListener);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Message msg = getItem(position);

        if (position > latestViewedMsgIndex) {
            latestViewedMsgIndex = position;
            latestViewedMsgId = msg.getMessageId();

            ChatDatastore.getInstance().updateLastViewedMessage(conversationId, latestViewedMsgId);
        }

        switch (getItemViewTypeEnum(position)) {
        case HIDDEN:
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.chat_message_ignore, parent, false);
            }
            break;

        case BANNER:
        {
            ChatBannerViewHolder viewHolder;
            if (convertView != null) {
                viewHolder = (ChatBannerViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.chat_message_banner, parent, false);
                viewHolder = new ChatBannerViewHolder(convertView, spannableCache);
                convertView.setTag(viewHolder);
            }
            viewHolder.setData(msg);
            break;
        }

        case NOTIFICATION:
            convertView = setupChatNotificationViewHolder(convertView, parent, position, R.layout.chat_message);
            break;

        case INCOMING:
            convertView = setupMessageViewHolder(convertView, parent, position, R.layout.chat_message_incoming);
            break;

        case OUTGOING:
            convertView = setupMessageViewHolder(convertView, parent, position, R.layout.chat_message_outgoing);
            break;

        case INCOMING_GIFT:
            convertView = setupGiftMessageViewHolder(convertView, parent, position, R.layout.chat_message_gift_incoming);
            break;

        case OUTGOING_GIFT:
            convertView = setupGiftMessageViewHolder(convertView, parent, position, R.layout.chat_message_gift_outgoing);
            break;
        }

        return convertView;
    }

    public void setMessagesList(@NonNull List<Message> messagesListData) {
        messagesList = messagesListData;
        notifyDataSetChanged();
    }

    private long getMessageTimestampAtIndex(int index) {
        Message message = getItem(index);
        return message.getLongTimestamp();
    }

    private boolean displayDateTitleWithMessage(int index) {
        if (index == 0) {
            return true;
        }

        Message currMessage = getItem(index);

        if (!Tools.hasSameDayTimestamp(currMessage.getLongTimestamp(), getMessageTimestampAtIndex(index - 1))) {
            return true;
        }

        return false;
    }

    private boolean displayUnreadMessageSeparator(int currIndex) {
        if (!getItem(currIndex).isIncoming())
            return false;

        if (currIndex == 0)
            return false;

        if (currIndex == latestViewedMsgIndexLastTime + 1) {
            return true;
        }

        return false;
    }

    private boolean canCollapseWithPrevMessage(int currPosition) {
        if (currPosition == 0)
            return false;

        if (getItem(currPosition).hasOwnMention() && getItem(currPosition).isIncoming())
            return false;

        Message.Type previousItemViewType = getItemViewTypeEnum(currPosition-1);
        if (previousItemViewType != Message.Type.INCOMING &&
                previousItemViewType != Message.Type.OUTGOING) {
            return false;
        }

        Message prevMessage = getItem(currPosition - 1);
        Message currMessage = getItem(currPosition);

        if (prevMessage.getSender() == null || currMessage.getSender() == null) {
            return false;
        }

        if (!prevMessage.getSender().equals(currMessage.getSender())) {
            return false;
        }

        // if time difference between messages is more than 1 minute, do not
        // collapse
        if ((currMessage.getLongTimestamp() - prevMessage.getLongTimestamp()) > Constants.CHAT_CONV_TIMESTAMP_DISPLAY) {
            return false;
        }

        // if messages are from different days, do not collapse
        if (!Tools.hasSameDayTimestamp(currMessage.getLongTimestamp(), prevMessage.getLongTimestamp())) {
            return false;
        }

        return true;
    }

    public void setMessageItemClickListener(BaseViewListener<Message> listener) {
        this.listener = listener;
    }

    public void setMimeItemLongClickListener(MimeContentViewListener mimeClickListener) {
        mimeListener = mimeClickListener;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setIsMigPrivateChat(boolean isMigPrivateChat) {
        this.isMigPrivateChat = isMigPrivateChat;
    }

    public void setLatestViewedMessageLastTime(String latestViewedMsgIdLastTime) {
        this.latestViewedMsgIdLastTime = latestViewedMsgIdLastTime;
        this.latestViewedMsgId = latestViewedMsgIdLastTime;
        updateLatestViewedMessageIndex();
    }

    /**
     *  update the message index by the id.  message index would change after loading previous messages,
     *  but id would not change
     */
    public void updateLatestViewedMessageIndex() {
        ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (chatConversation == null) {
            return;
        }
        
        int index = chatConversation.getPositionOfMessage(chatConversation.getMessage(latestViewedMsgId));
        if (index > latestViewedMsgIndex) {
            Logger.debug.log("Dangui", "update latestViewedMsgIndex from:" + this.latestViewedMsgIndex + " to " + index);
            this.latestViewedMsgIndex = index;
        }
        index = chatConversation.getPositionOfMessage(chatConversation.getMessage(latestViewedMsgIdLastTime));
        Logger.debug.log("Dangui", "update latestViewedMsgIndexLastTime from:" + this.latestViewedMsgIndexLastTime
                + " to " + index);
        this.latestViewedMsgIndexLastTime = index;
    }
    
    public int getLatestViewedMessageIndex() {
        return this.latestViewedMsgIndexLastTime;
    }
	
    /**
     * @return
     */
    public boolean isLastViewedMessageChanged() {
        if (latestViewedMsgIdLastTime == null && latestViewedMsgId != null) {
            return true;
        }
        
        if (latestViewedMsgIdLastTime != null && latestViewedMsgId != null 
                && !latestViewedMsgId.equals(latestViewedMsgIdLastTime)) {
            return true;
        }
        
        return false;
    }

    public static <E> boolean containsInstance(List<E> list, Class<? extends E> clazz) {
        for (E e : list) {
            if (clazz.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    public ConcurrentHashMap<String, SpannableStringBuilder> getSpannableCache() {
        return spannableCache;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        Object tag = view.getTag();
        if(tag != null && tag instanceof BaseMessageViewHolder) {
            Logger.debug.log("MessageListAdapater", "Class: ", tag.getClass().getSimpleName());
            BaseMessageViewHolder holder = (BaseMessageViewHolder) tag;
            holder.onMovedToScrapHeap(this);

        }
    }

    @Override
    public void setObjectForKey(String key, Object object) {
        // TODO
    }

    @Override
    public void getObjectForKey(String key) {
        // TODO
    }

    @Override
    public void deleteObjectForKey(String key) {
        // TODO
    }
}
