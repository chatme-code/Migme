/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftRecipientAdapter.java
 * Created 19 May, 2014, 11:31:39 am
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.GiftRecipientViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dan
 */

public class GiftRecipientAdapter extends BaseAdapter {

    private List<ChatParticipant>             participantListData = new ArrayList<ChatParticipant>();
    private LayoutInflater                    mInflater;
    private BaseViewListener<ChatParticipant> participantClickListener;

    private String                            mSelectedRecipient;

    public GiftRecipientAdapter() {
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (participantListData != null) {
            return participantListData.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (participantListData != null) {
            return participantListData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setParticipantsList(List<ChatParticipant> participantListData) {
        this.participantListData = participantListData;
        notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GiftRecipientViewHolder recipientViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gift_recipient_item, null);
            recipientViewHolder = new GiftRecipientViewHolder(convertView);
            convertView.setTag(R.id.holder, recipientViewHolder);
        } else {
            recipientViewHolder = (GiftRecipientViewHolder) convertView.getTag(R.id.holder);
        }

        ChatParticipant chatParticipant = (ChatParticipant) getItem(position);
        if (chatParticipant != null) {
            recipientViewHolder.setSelected(mSelectedRecipient);
            recipientViewHolder.setData(chatParticipant);
            recipientViewHolder.setBaseViewListener(participantClickListener);
        }

        return convertView;
    }

    public void setParticipantClickListener(BaseViewListener<ChatParticipant> listener) {
        this.participantClickListener = listener;
    }

    public void setSelectedParticipant(String selectedRecipient) {
        mSelectedRecipient = selectedRecipient;
    }

}
