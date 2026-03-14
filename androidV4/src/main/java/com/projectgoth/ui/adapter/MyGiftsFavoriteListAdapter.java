package com.projectgoth.ui.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.ui.holder.MyGiftsFavoriteListViewHolder;

import java.util.List;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsFavoriteListAdapter extends BaseAdapter {

    private final int DISPLAY_MAX_COUNT = 3;
    private LayoutInflater           mInflater;

    private List<GiftMimeData>       mItemList;

    public MyGiftsFavoriteListAdapter(FragmentActivity fragment) {
        super();
        mInflater = LayoutInflater.from(fragment);
    }

    @Override
    public int getCount() {
        if (mItemList != null) {
            return mItemList.size() > DISPLAY_MAX_COUNT ? DISPLAY_MAX_COUNT : mItemList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mItemList != null && position < mItemList.size()) {
            return mItemList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyGiftsFavoriteListViewHolder giftCardViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_my_gift_favorite, null);
            giftCardViewHolder = new MyGiftsFavoriteListViewHolder(convertView);
            convertView.setTag(R.id.holder, giftCardViewHolder);

        } else {
            giftCardViewHolder = (MyGiftsFavoriteListViewHolder) convertView.getTag(R.id.holder);
        }

        GiftMimeData giftListItem = (GiftMimeData) getItem(position);
        if (giftListItem != null) {
            giftCardViewHolder.setData(giftListItem);
        }

        return convertView;
    }

    public void setItemList(List<GiftMimeData> giftList) {
        mItemList = giftList;
        notifyDataSetChanged();
    }
}
