package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.holder.MyStickerPackViewHolder;

/**
 * Created by houdangui on 15/12/14.
 */
public class MyStickerPackAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<BaseEmoticonPackData> stickerPackList = new ArrayList<BaseEmoticonPackData>();

    private BaseViewHolder.BaseViewListener<BaseEmoticonPackData> listener;

    public MyStickerPackAdapter() {
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        int count = 0;
        if (stickerPackList != null) {
            count = stickerPackList.size();
        }
        return count;
    }

    @Override
    public Object getItem(int pos) {
        if (stickerPackList != null) {
            return stickerPackList.get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {

        MyStickerPackViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
           convertView = mInflater.inflate(R.layout.holder_my_sticker_pack, null);
           holder =  new MyStickerPackViewHolder(convertView);
        } else {
           holder = (MyStickerPackViewHolder) convertView.getTag();
        }

        BaseEmoticonPackData stickerPack = (BaseEmoticonPackData)getItem(pos);
        holder.setData(stickerPack);
        holder.setBaseViewListener(listener);

        return convertView;
    }

    public void setStickerPackList(List<BaseEmoticonPackData> stickerPackList) {
        this.stickerPackList = stickerPackList;
        notifyDataSetChanged();
    }

    public void setListener(BaseViewHolder.BaseViewListener<BaseEmoticonPackData> listener) {
        this.listener = listener;
    }
}
