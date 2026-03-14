package com.projectgoth.ui.holder;

import android.text.SpannableStringBuilder;
import android.view.View;

import com.projectgoth.controller.ChatroomColorController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.ui.widget.TextViewEx;

import java.util.concurrent.ConcurrentHashMap;

public class ChatBannerViewHolder extends BaseViewHolder<Message> {

    private final TextViewEx      textView;

    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;

    public ChatBannerViewHolder(View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        super(view);
        this.spannableCache = spannableCache;
        this.textView = (TextViewEx) view;
    }

    @Override
    public void setData(Message message) {
        super.setData(message);

        String conversationId = message.getConversationId();
        ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (chatConversation != null && chatConversation.isChatroom()) {
            int chatroomColor = ChatroomColorController.getInstance().getChatroomColor(chatConversation.getChatId());
            textView.setBackgroundColor(chatroomColor);
        }

        textView.setHotkeysFromServer(message.getHotkeys());
        textView.setText(message.getMessage(), spannableCache);
    }

}
