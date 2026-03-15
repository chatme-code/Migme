package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deezer.sdk.model.Track;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.MusicData;
import com.projectgoth.b.data.MusicGenreData;
import com.projectgoth.b.data.MusicItem;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.music.deezer.BaseDeezerPlaybackFragment;
import com.projectgoth.music.deezer.DeezerPlayback;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.adapter.NewMusicAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.HorizontalListViewEx;
import com.projectgoth.ui.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by mapet on 17/4/15.
 */
public class MusicFragment extends BaseDeezerPlaybackFragment implements BaseViewListener<MusicItem>,
        DeezerPlayback.DeezerPlayerActionListener, View.OnClickListener, MusicGenreFilterFragment.MusicGenreFilterListener,
        HorizontalListViewEx.OnItemClickListener {

    private HorizontalListViewEx mMigSelectionList;
    private Spinner mGenreSelector;
    private ImageView mArrowDown;
    private FrameLayout mMigSelectionEmptyView;
    private FrameLayout mDeezerSelectionEmptyView;

    private GridView mChannelList;

    private NewMusicAdapter mMigmeSelectionAdapter;
    private NewMusicAdapter mDeezerSelectionAdapter;

    private ArrayList<MusicItem> mMigStations;
    private ArrayList<MusicItem> mDeezerStations;

    private DeezerPlayback mPlayerUI;
    private View mProgressBar;

    private int mGenreSelectedId = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_music;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setMode(Mode.FILTERABLE);

        mMigSelectionEmptyView = (FrameLayout) view.findViewById(R.id.header_view_container);
        mDeezerSelectionEmptyView = (FrameLayout) view.findViewById(R.id.empty_view_container);

        showOrHideLoadingIcon();

        TextView migSelectionLabel = (TextView) view.findViewById(R.id.mig_selection_label);
        migSelectionLabel.setText(I18n.tr("mig selection"));
        mMigSelectionList = (HorizontalListViewEx) view.findViewById(R.id.mig_selection_list);
        mMigSelectionList.setOnItemClickListener(this);
        mArrowDown = (ImageView) view.findViewById(R.id.arrow_down);
        mArrowDown.setOnClickListener(this);

        mGenreSelector = (Spinner) view.findViewById(R.id.genre_container);
        mGenreSelector.setSpinnerLabel(I18n.tr("Select"));
        mGenreSelector.setOnClickListener(this);

        // prepare Player
        mPlayerUI = (DeezerPlayback) view.findViewById(R.id.deezer_playback);
        mPlayerUI.setDeezerPlayerActionListener(this);
        mPlayerUI.setOnClickListener(this);
        mProgressBar = (LinearLayout) getActivity().findViewById(R.id.progressBarLayout);

        mChannelList = (GridView) view.findViewById(R.id.channel_list);

        mChannelList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    hideMigSelection();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mMigmeSelectionAdapter = new NewMusicAdapter();
        mDeezerSelectionAdapter = new NewMusicAdapter();

        initDataCache();

        mMigSelectionList.setAdapter(mMigmeSelectionAdapter);
        mChannelList.setAdapter(mDeezerSelectionAdapter);
    }

    @Override
    protected void onShowFragment() {
        super.onShowFragment();
        initDataCache();
    }

    private void initDataCache() {
        updateMigStations();
        updateDeezerStations();
        MusicDatastore.getInstance().getFavoriteMusicChannels(Session.getInstance().getUserId());
        MusicDatastore.getInstance().getAllMusicGenreData();
    }

    private void updateMigStations() {
        MusicData migMusicData = MusicDatastore.getInstance().getMigmeStationsData();
        if (migMusicData != null) {
            mMigStations = new ArrayList<MusicItem>(Arrays.asList(migMusicData.getData()));

            HorizontalScrollView.LayoutParams lp = new HorizontalScrollView.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            mMigSelectionList.setItemLayoutParams(lp);

            mMigmeSelectionAdapter.setShowChannelSource(false);
            mMigmeSelectionAdapter.setMusicItemList(mMigStations);
            mMigSelectionList.setAdapter(mMigmeSelectionAdapter);
        }
        showOrHideLoadingIcon();
    }

    private void updateDeezerStations() {
        mDeezerStations = MusicDatastore.getInstance().getAllDeezerStations();
        if (mDeezerStations != null) {
            mDeezerSelectionAdapter.setShowChannelSource(true);
            mDeezerSelectionAdapter.setMusicItemList(mDeezerStations);
            mDeezerSelectionAdapter.setMusicItemListener(this);
        }
        showOrHideLoadingIcon();
    }

    private void updateFavoriteStations() {
        HashSet<MusicItem> favoriteStations = MusicDatastore.getInstance().getFavoriteStations();
        if (mDeezerStations != null) {
            mDeezerSelectionAdapter.setShowChannelSource(true);
            mDeezerSelectionAdapter.setMusicItemList(new ArrayList<MusicItem>(favoriteStations));
            mDeezerSelectionAdapter.setMusicItemListener(this);
        }
        showOrHideLoadingIcon();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Music.FETCH_MIGME_STATIONS_COMPLETED);
        registerEvent(Events.Music.FETCH_DEEZER_STATIONS_COMPLETED);
        registerEvent(Events.Music.FETCH_DEEZER_STATION_BY_GENRE_COMPLETED);
        registerEvent(Events.UserFavorite.FETCH_USER_FAVORITES_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Music.FETCH_MIGME_STATIONS_COMPLETED)) {
            updateMigStations();
        } else if (action.equals(Events.Music.FETCH_DEEZER_STATIONS_COMPLETED)) {
            mGenreSelector.setSpinnerLabel(I18n.tr("All categories"));
            updateDeezerStations();
        } else if (action.equals(Events.Music.FETCH_DEEZER_STATION_BY_GENRE_COMPLETED)) {
            updateDeezerStationsByGenre(mGenreSelectedId);
        } else if (action.equals(Events.UserFavorite.FETCH_USER_FAVORITES_COMPLETED)) {
            updateMigStations();
            updateDeezerStationsByGenre(mGenreSelectedId);
        }
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Music");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_music_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(CustomActionBarConfig.NavigationButtonState.BACK);
        return config;
    }

    @Override
    public void onPlayTrack(Track track) {
        if (mDeezerPlayback != null) {
            mDeezerPlayback.setTrackInfo(track);
            mDeezerPlayback.updateUIByPlayState(DeezerPlayerManager.getInstance().getPlayerState());
        }
    }

    @Override
    public void onTrackEnded(Track track) {
    }

    @Override
    public void onRequestException(Exception e, Object o) {
    }

    @Override
    public void onItemClick(View v, MusicItem data) {
        if (data != null) {
            playMusic(data.getId());
        }
    }

    @Override
    public void onItemLongClick(View v, MusicItem data) {
    }

    @Override
    public void onItemClicked(HorizontalListViewEx adapterView, View view, int position, long id) {
        MusicItem musicItem = mMigStations.get(position);
        playMusic(musicItem.getId());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deezer_playback:
                ActionHandler.getInstance().displayMusicDetailPage(getActivity());
                break;
            case R.id.genre_container:
                ActionHandler.getInstance().displayMusicGenreFilterFragment(getActivity(), mGenreSelectedId, this);
                break;
            case R.id.arrow_down:
                showMigSelection();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerStop() {
        mPlayerUI.setVisibility(View.GONE);
        DeezerPlayerManager.getInstance().stop();
        DeezerPlayerManager.getInstance().clearBgPlayingRadio();
    }

    public void onBackIconPressed() {
        if (DeezerPlayerManager.getInstance().getBgPlayingRadioId() > 0) {
            NativeNotificationManager.getInstance().addListerToPlayer();
            NativeNotificationManager.getInstance().displayNotificationBar(null);
        }
        super.onBackIconPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        // do not show the playback UI if radio player is stopped
        if (!DeezerPlayerManager.getInstance().isPlaying()) {
            mPlayerUI.setVisibility(View.INVISIBLE);
        }
        // play cached music
        if (DeezerPlayerManager.getInstance().getBgPlayingRadioId() > 0) {
            mPlayerUI.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            DeezerPlayerManager.getInstance().playRadio(DeezerPlayerManager.getInstance().getBgPlayingRadioId());
            Track track = DeezerPlayerManager.getInstance().getBgPlayingTrack();
            GAEvent.Deezer_LandingFragment.send(DeezerPlayerManager.getInstance().getBgPlayingRadioId());
            mDeezerPlayback.setTrackInfo(track);
        }
    }

    @Override
    public void onGenreSelected(MusicGenreData musicGenreData) {
        mGenreSelectedId = musicGenreData.getId().intValue();
        mGenreSelector.setSpinnerLabel(musicGenreData.getTitle());

        if (mGenreSelectedId == MusicGenreFilterFragment.CustomGenreFilterType.FAVORITES.ordinal()) {
            updateFavoriteStations();
        } else if (mGenreSelectedId == MusicGenreFilterFragment.CustomGenreFilterType.ALL_CATEGORIES.ordinal()) {
            updateDeezerStations();
        } else {
            updateDeezerStationsByGenre(mGenreSelectedId);
        }

        dismissDeezerSelectionLoadingView();
    }

    private void updateDeezerStationsByGenre(int genreId) {
        ArrayList<MusicItem> deezerStationsInGenre = MusicDatastore.getInstance().getMusicStationsDataByGenre(genreId);
        if (deezerStationsInGenre != null) {
            mDeezerSelectionAdapter.setShowChannelSource(true);
            mDeezerSelectionAdapter.setMusicItemList(deezerStationsInGenre);
            mDeezerSelectionAdapter.setMusicItemListener(this);
        }
    }

    @Override
    protected void performFilter(String filterString) {
        super.performFilter(filterString);
        mMigmeSelectionAdapter.filterAndRefresh(filterString);
        mMigSelectionList.setAdapter(mMigmeSelectionAdapter);
        mDeezerSelectionAdapter.filterAndRefresh(filterString);
    }

    private void playMusic(String musicItemId) {
        mPlayerUI.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        long radioId = Long.valueOf(musicItemId);
        DeezerPlayerManager.getInstance().setBgPlayingRadioId(radioId);
        DeezerPlayerManager.getInstance().playRadio(radioId);
        GAEvent.Deezer_LandingFragment.send(radioId);
    }

    private void hideMigSelection() {
        mMigSelectionList.setVisibility(View.GONE);
        mArrowDown.setVisibility(View.VISIBLE);
    }

    private void showMigSelection() {
        mMigSelectionList.setVisibility(View.VISIBLE);
        mArrowDown.setVisibility(View.GONE);
    }

    private void showOrHideLoadingIcon() {
        if (mChannelList == null || mDeezerSelectionAdapter.getCount() <= 0) {
            showDeezerSelectionLoadingView();
        } else {
            dismissDeezerSelectionLoadingView();
        }

        if (mMigSelectionList == null || mMigmeSelectionAdapter.getCount() <= 0) {
            showMigmeSelectionLoadingView();
        } else {
            dismissMigmeSelectionLoadingView();
        }
    }

    private void showDeezerSelectionLoadingView() {
        if (mDeezerSelectionEmptyView.getVisibility() == View.VISIBLE) {
            return;
        }
        mDeezerSelectionEmptyView.addView(getEmptyView());
        mDeezerSelectionEmptyView.setVisibility(View.VISIBLE);
    }

    private void dismissDeezerSelectionLoadingView() {
        mDeezerSelectionEmptyView.removeAllViews();
        mDeezerSelectionEmptyView.setVisibility(View.GONE);
    }

    private void showMigmeSelectionLoadingView() {
        if (mMigSelectionEmptyView.getVisibility() == View.VISIBLE) {
            return;
        }
        mMigSelectionEmptyView.addView(getEmptyView());
        mMigSelectionEmptyView.setVisibility(View.VISIBLE);
    }

    private void dismissMigmeSelectionLoadingView() {
        mMigSelectionEmptyView.removeAllViews();
        mMigSelectionEmptyView.setVisibility(View.GONE);
    }

    private View getEmptyView() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_loading, null);
        ImageView loadingIcon = (ImageView) emptyView.findViewById(R.id.loading_icon);
        loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));
        return emptyView;
    }

}
