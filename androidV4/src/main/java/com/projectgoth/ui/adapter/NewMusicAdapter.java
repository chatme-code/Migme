package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.MusicItem;
import com.projectgoth.common.TextUtils;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.content.NewMusicViewHolder;
import com.projectgoth.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by mapet on 17/4/15.
 */
public class NewMusicAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<MusicItem> mMusicItemList;
    private ArrayList<MusicItem> mUnfilteredMusicItemList;
    private boolean mShouldShowSource;

    private BaseViewListener<MusicItem> mMusicItemListener;

    public NewMusicAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mMusicItemList != null) {
            return mMusicItemList.size();
        }
        return 0;
    }

    @Override
    public MusicItem getItem(int position) {
        if (mMusicItemList != null && position < mMusicItemList.size()) {
            return mMusicItemList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NewMusicViewHolder musicViewHolder;

        if (convertView == null) {
            if (mShouldShowSource) {
                convertView = mInflater.inflate(R.layout.holder_item_music_new, null);
            } else {
                convertView = mInflater.inflate(R.layout.holder_item_mig_selection, null);
            }
            musicViewHolder = new NewMusicViewHolder(convertView, mShouldShowSource);
            convertView.setTag(R.id.holder, musicViewHolder);
        } else {
            musicViewHolder = (NewMusicViewHolder) convertView.getTag(R.id.holder);
        }

        MusicItem musicItem = getItem(position);
        if (musicItem != null) {
            musicViewHolder.setData(musicItem);
            musicViewHolder.setBaseViewListener(mMusicItemListener);
        }

        return convertView;
    }

    public void setMusicItemList(ArrayList<MusicItem> musicItemList) {
        mMusicItemList = musicItemList;
        mUnfilteredMusicItemList = musicItemList;
        notifyDataSetChanged();
    }

    public void setMusicItemListener(BaseViewListener<MusicItem> musicItemListener) {
        mMusicItemListener = musicItemListener;
    }

    public void filterAndRefresh(final String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            setFilteredData(mUnfilteredMusicItemList);
        } else {
            final ArrayList<MusicItem> filteredData = getFilteredData(filterText);
            setFilteredData(filteredData);
        }
    }

    private ArrayList<MusicItem> getFilteredData(final String filterText) {
        ArrayList<MusicItem> resultList = new ArrayList<MusicItem>();

        if (mUnfilteredMusicItemList != null) {
            for (MusicItem item : mUnfilteredMusicItemList) {
                MusicItem musicItem = (MusicItem) item;
                final String musicItemName = item.getTitle();
                if (StringUtils.containsIgnoreCase(musicItemName, filterText)) {
                    resultList.add(musicItem);
                }
            }
        }

        return resultList;
    }

    public void setFilteredData(final ArrayList<MusicItem> data) {
        if (data != null) {
            this.mMusicItemList = data;
            notifyDataSetChanged();
        }
    }

    public void setShowChannelSource(boolean shouldShowSource) {
        mShouldShowSource = shouldShowSource;
    }

}
