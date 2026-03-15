package com.projectgoth.ui.adapter;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.Session;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ConversationViewHolder;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListAdapter extends BaseAdapter {

    private ArrayList<ChatConversation>         mConversationList = new ArrayList<ChatConversation>();
    private ArrayList<ChatConversation>         mFilteredList = new ArrayList<ChatConversation>();
    private LayoutInflater                      mInflater = LayoutInflater.from(ApplicationEx.getContext());
    private BaseViewListener<ChatConversation>  mListener;
    private String                              mFilter = null;
    private boolean                             isForSelectChat;
    
    private ConcurrentHashMap<String, SpannableStringBuilder> mSpannableCache =
            new ConcurrentHashMap<String, SpannableStringBuilder>();

    
    public ChatListAdapter(ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        mSpannableCache = spannableCache;
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mFilteredList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parentView) {

        ConversationViewHolder conversationViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_chat_list_item, null);
            conversationViewHolder = new ConversationViewHolder(convertView, mSpannableCache, isForSelectChat);
            convertView.setTag(R.id.holder, conversationViewHolder);
        } else {
            conversationViewHolder = (ConversationViewHolder) convertView.getTag(R.id.holder);
        }

        ChatConversation item = (ChatConversation) getItem(pos);
        conversationViewHolder.setData(item);
        conversationViewHolder.setBaseViewListener(mListener);

        return convertView;
    }

    public void setChatItemListener(BaseViewListener<ChatConversation> listener) {
        mListener = listener;
    }

    public void setChatList(ArrayList<ChatConversation> chatList) {
        mConversationList = chatList;
        updateFilteredList();
    }

    public void setFilter(String filter) {
        mFilter = filter.toLowerCase();
        
        updateFilteredList();
        notifyDataSetChanged();
    }
    
    public String getFilter() {
        return mFilter;
    }
    
    private void updateFilteredList() {
        if(Session.getInstance().isBlockUsers()) {
            //do not show chat list after logout and peek inside
            mFilteredList = new ArrayList<ChatConversation>();
            return;
        }

        if (TextUtils.isEmpty(mFilter)) {
            if (mFilteredList != mConversationList) {
                mFilteredList = new ArrayList<ChatConversation>(mConversationList);
            }
        } else {
            mFilteredList.clear();
            for (ChatConversation conversation : mConversationList) {
                if (conversation.getDisplayName().toLowerCase().contains(mFilter)) {
                    mFilteredList.add(conversation);
                }
            }
        }
    }

    public void setForSelectChat(boolean isForShareInChat) {
        this.isForSelectChat = isForShareInChat;
    }

}
