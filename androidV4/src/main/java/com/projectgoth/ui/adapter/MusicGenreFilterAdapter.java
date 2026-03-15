package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.MusicGenreData;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.MusicGenreFilterViewHolder;

import java.util.ArrayList;

/**
 * Created by mapet on 21/4/15.
 */
public class MusicGenreFilterAdapter extends BaseAdapter {

    private ArrayList<MusicGenreData> mDataList;
    private LayoutInflater mInflater;
    private BaseViewListener<MusicGenreData> mListener;
    private int mSelectedGenreId;

    public MusicGenreFilterAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public MusicGenreData getItem(int position) {
        if (mDataList != null && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicGenreFilterViewHolder filterListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_music_genre_filter_item, null);
            filterListViewHolder = new MusicGenreFilterViewHolder(convertView);
            convertView.setTag(R.id.holder, filterListViewHolder);

        } else {
            filterListViewHolder = (MusicGenreFilterViewHolder) convertView.getTag(R.id.holder);
        }

        MusicGenreData musicGenreData = (MusicGenreData) getItem(position);
        if (musicGenreData != null) {
            filterListViewHolder.setBaseViewListener(mListener);
            filterListViewHolder.setSelectedGenreId(mSelectedGenreId);
            filterListViewHolder.setData(musicGenreData);
        }

        return convertView;
    }

    public void setList(ArrayList<MusicGenreData> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public void setMusicGenreFilterListener(BaseViewListener<MusicGenreData> listener) {
        this.mListener = listener;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        mSelectedGenreId = selectedGenreId;
    }
}
