/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerPackDetailsAdapter.java
 * Created Dec 16, 2014, 6:06:50 PM
 */

package com.projectgoth.ui.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.holder.StickerPackDetailsViewHolder;

/**
 * @author mapet
 * 
 */
public class StickerPackDetailsAdapter extends BaseAdapter {

    private List<Sticker>  stickerList;
    private LayoutInflater mInflater;

    public StickerPackDetailsAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    public void setGridList(List<Sticker> stickerList) {
        this.stickerList = stickerList;
    }

    @Override
    public int getCount() {
        return stickerList.size();
    }

    @Override
    public Sticker getItem(int position) {
        return stickerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Sticker sticker = getItem(position);
        StickerPackDetailsViewHolder stickerViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_item_sticker, null);
            stickerViewHolder = new StickerPackDetailsViewHolder(convertView);
            convertView.setTag(R.id.holder, stickerViewHolder);

        } else {
            stickerViewHolder = (StickerPackDetailsViewHolder) convertView.getTag(R.id.holder);
        }

        stickerViewHolder.setData(sticker);
        return convertView;
    }

}
