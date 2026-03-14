package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.MusicGenreData;
import com.projectgoth.common.Config;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.adapter.MusicGenreFilterAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

import java.util.ArrayList;

/**
 * Created by mapet on 21/4/15.
 */
public class MusicGenreFilterFragment extends BaseDialogFragment implements BaseViewListener<MusicGenreData> {

    public static final String PARAM_SELECTED_FILTER = "PARAM_SELECTED_FILTER";
    private ListView mList;
    private MusicGenreFilterAdapter mAdapter;
    private ArrayList<MusicGenreData> mMusicGenreDataList;
    private int mSelectedGenreId;
    private MusicGenreFilterListener mListener;

    public interface MusicGenreFilterListener {

        public void onGenreSelected(MusicGenreData musicGenreData);

    }

    public enum CustomGenreFilterType {
        FAVORITES("Favorite channels"), ALL_CATEGORIES("All categories");

        private String type;

        private CustomGenreFilterType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public static CustomGenreFilterType fromValue(String type) {
            for (CustomGenreFilterType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return ALL_CATEGORIES;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_gifts_overview_filter;
    }

    @Override
    protected void readBundleArguments(Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);
        mSelectedGenreId = bundleArgs.getInt(PARAM_SELECTED_FILTER);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.list_view);
        mList.addHeaderView(createHeader());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mList.getLayoutParams();
        lp.height = (int) (Config.getInstance().getScreenHeight() * 0.6f);
        lp.width = (int) (Config.getInstance().getScreenWidth());
        mList.setLayoutParams(lp);

        mAdapter = new MusicGenreFilterAdapter();
        setListData();
    }

    private void setListData() {
        ArrayList<MusicGenreData> completeGenreList = new ArrayList<MusicGenreData>();

        // Add custom genres
        MusicGenreData mgd = new MusicGenreData();

        mgd.setId(CustomGenreFilterType.FAVORITES.ordinal());
        mgd.setTitle(CustomGenreFilterType.FAVORITES.getType());
        completeGenreList.add(mgd);

        mgd = new MusicGenreData();
        mgd.setId(CustomGenreFilterType.ALL_CATEGORIES.ordinal());
        mgd.setTitle(CustomGenreFilterType.ALL_CATEGORIES.getType());
        completeGenreList.add(mgd);

        mMusicGenreDataList = MusicDatastore.getInstance().getAllMusicGenreData();

        if (mMusicGenreDataList != null) {
            completeGenreList.addAll(mMusicGenreDataList);
            mAdapter.setList(completeGenreList);
            mAdapter.setMusicGenreFilterListener(this);
            mAdapter.setSelectedGenreId(mSelectedGenreId);
            mList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    private View createHeader() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_music_genre_filter, null);
        TextView headerText = (TextView) header.findViewById(R.id.label);
        headerText.setText(I18n.tr("Select"));
        return header;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Music.FETCH_DEEZER_STATIONS_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Music.FETCH_DEEZER_STATIONS_COMPLETED)) {
            setListData();
        }
    }

    @Override
    public void onItemClick(View v, MusicGenreData data) {
        if (mListener != null) {
            mListener.onGenreSelected(data);
            closeFragment();
        }
    }

    @Override
    public void onItemLongClick(View v, MusicGenreData data) {
    }

    public void setMusicGenreFilterListener(MusicGenreFilterListener listener) {
        this.mListener = listener;
    }

}
