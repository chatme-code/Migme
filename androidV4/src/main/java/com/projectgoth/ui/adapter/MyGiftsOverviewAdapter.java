/**
 * Copyright (c) 2013 Project Goth
 *
 * MyGiftsOverviewAdapter.java
 * Created Jan 23, 2015, 10:45:48 AM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftReceivedLeaderboardItem;
import com.projectgoth.b.data.GiftSenderLeaderboardItem;
import com.projectgoth.model.MyGiftsOverviewData;
import com.projectgoth.ui.fragment.MyGiftsOverviewFragment.MyGiftsOverviewDisplayType;
import com.projectgoth.ui.holder.MyGiftsOverviewReceivedViewHolder;
import com.projectgoth.ui.holder.MyGiftsOverviewSenderViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * @param <T>
 * 
 */
public class MyGiftsOverviewAdapter<T> extends BaseAdapter {

    private LayoutInflater                                mInflater;
    private MyGiftsOverviewData<T>                        overviewData;

    private BaseViewListener<GiftSenderLeaderboardItem>   senderLeaderboardListener;
    private BaseViewListener<GiftReceivedLeaderboardItem> receivedLeaderboardListener;

    public MyGiftsOverviewAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (overviewData != null && overviewData.getListData() != null) {
            return overviewData.getListData().size();
        }
        return 0;
    }

    @Override
    public Object getItem(int pos) {
        if (overviewData != null && pos < getCount()) {
            return overviewData.getListData().get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (overviewData.getDisplayType() == MyGiftsOverviewDisplayType.SENDER) {
            MyGiftsOverviewSenderViewHolder myGiftsOverviewSenderViewHolder;

            if (convertView == null || !(convertView.getTag() instanceof MyGiftsOverviewSenderViewHolder)) {
                convertView = mInflater.inflate(R.layout.holder_my_gifts_overview, null);
                myGiftsOverviewSenderViewHolder = new MyGiftsOverviewSenderViewHolder(convertView, pos + 1);
                convertView.setTag(myGiftsOverviewSenderViewHolder);
            } else {
                myGiftsOverviewSenderViewHolder = (MyGiftsOverviewSenderViewHolder) convertView.getTag();
            }

            GiftSenderLeaderboardItem senderItem = (GiftSenderLeaderboardItem) getItem(pos);
            myGiftsOverviewSenderViewHolder.setData(senderItem);
            myGiftsOverviewSenderViewHolder.setBaseViewListener(senderLeaderboardListener);

        } else if (overviewData.getDisplayType() == MyGiftsOverviewDisplayType.RECEIVED) {
            MyGiftsOverviewReceivedViewHolder myGiftsOverviewReceivedViewHolder;

            if (convertView == null || !(convertView.getTag() instanceof MyGiftsOverviewReceivedViewHolder)) {
                convertView = mInflater.inflate(R.layout.holder_my_gifts_overview, null);
                myGiftsOverviewReceivedViewHolder = new MyGiftsOverviewReceivedViewHolder(convertView, pos + 1);
                convertView.setTag(myGiftsOverviewReceivedViewHolder);
            } else {
                myGiftsOverviewReceivedViewHolder = (MyGiftsOverviewReceivedViewHolder) convertView.getTag();
            }

            GiftReceivedLeaderboardItem receivedItem = (GiftReceivedLeaderboardItem) getItem(pos);
            myGiftsOverviewReceivedViewHolder.setData(receivedItem);
            myGiftsOverviewReceivedViewHolder.setBaseViewListener(receivedLeaderboardListener);
        }

        return convertView;
    }

    public void setLeaderboardData(MyGiftsOverviewData<T> data) {
        overviewData = data;
        notifyDataSetChanged();
    }

    public void setSenderListener(BaseViewListener<GiftSenderLeaderboardItem> senderLeaderboardListener) {
        this.senderLeaderboardListener = senderLeaderboardListener;
    }

    public void setReceivedListener(BaseViewListener<GiftReceivedLeaderboardItem> receivedLeaderboardListener) {
        this.receivedLeaderboardListener = receivedLeaderboardListener;
    }

}
