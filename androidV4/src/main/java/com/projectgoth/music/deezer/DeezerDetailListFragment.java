
package com.projectgoth.music.deezer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.event.PlayerState;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.DeezerDatastore;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.RadioTrackAdaper;
import com.projectgoth.util.AndroidLogger;

import java.util.ArrayList;
import java.util.List;

public class DeezerDetailListFragment extends BaseDeezerPlaybackFragment {

    private static final String TAG = AndroidLogger.makeLogTag(DeezerDetailListFragment.class);
    private static final String DEEZER_HOME_PAGE = "http://www.deezer.com";
    private ImageView           mCategoryCover;
    private TextView            mCategoryName;
    private ListView            mTrackList;
    private ImageView           mFavoriteIcon;
    private DeezerRadio         mRadioData;
    private RadioTrackAdaper    mTrackAdapter;
    private long                mCurRadioId;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_deezer_radio_detail;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCategoryCover = (ImageView) view.findViewById(R.id.radio_category_cover);
        mCategoryName = (TextView) view.findViewById(R.id.radio_category_name);
        mCategoryName.setText(I18n.tr("Loading radio list"));

        ImageButton leftButton = (ImageButton) view.findViewById(R.id.playbackStop);
        leftButton.setImageResource(R.drawable.ic_action_previous_grey);

        mTrackList = (ListView) view.findViewById(R.id.deezer_track_list);
        mCurRadioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();

        ImageView shareIcon = (ImageView) view.findViewById(R.id.share_container);
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Constants.BLANKSTR;
                if (mRadioData != null) {
                    url = mRadioData.getShareUrl();
                }
                String strRadioId = String.valueOf(mCurRadioId);
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(strRadioId)) {
                    ShareManager.shareDeezerRadio(getActivity(), String.valueOf(mCurRadioId), url);
                }
            }
        });

        mFavoriteIcon = (ImageView) view.findViewById(R.id.favorite_container);
        String strRadioId = String.valueOf(mCurRadioId);
        if (!TextUtils.isEmpty(strRadioId)) {
            String musicChannelId = String.format("%s/%s/%s", MusicDatastore.MusicProviderType.deezer,
                    MusicDatastore.MusicChannelType.radio, strRadioId);

            if (MusicDatastore.getInstance().isFavoriteMusicChannel(musicChannelId)) {
                mFavoriteIcon.setImageResource(R.drawable.ad_favourite_pink);
            }
        }

        mFavoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strRadioId = String.valueOf(mCurRadioId);
                if (!TextUtils.isEmpty(strRadioId)) {
                    String musicChannelId = String.format("%s/%s/%s", MusicDatastore.MusicProviderType.deezer,
                            MusicDatastore.MusicChannelType.radio, strRadioId);

                    if (!MusicDatastore.getInstance().isFavoriteMusicChannel(musicChannelId)) {
                        MusicDatastore.getInstance().setFavoriteMusicChannel(musicChannelId, Session.getInstance().getUserId());
                    } else {
                        MusicDatastore.getInstance().removeFavoriteMusicChannel(musicChannelId, Session.getInstance().getUserId());
                    }
                }
            }
        });

        ImageView deezerBanner = (ImageView) view.findViewById(R.id.deezer_banner);
        deezerBanner.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DEEZER_HOME_PAGE));
                startActivity(browserIntent);
            }
        });

        mTrackAdapter = new RadioTrackAdaper();
        mTrackAdapter.setTrackList(new ArrayList<Track>());
        mTrackList.setAdapter(mTrackAdapter);

        // FIXME: Need to popup waiting cursor and wait for radio data
        requestRadioInfo();
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
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.DEEZER.FETCH_RADIO_COMPLETED);
        registerEvent(Events.DEEZER.FETCH_RADIO_ERROR);
        registerEvent(Events.UserFavorite.SET_FAVORITE_MUSIC_CHANNEL_COMPLETED);
        registerEvent(Events.UserFavorite.REMOVE_FAVORITE_MUSIC_CHANNEL_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.DEEZER.FETCH_RADIO_COMPLETED)) {
            long id = intent.getLongExtra(Events.DEEZER.Extra.RADIO_ID, -1);
            if (id == mCurRadioId) {
                mRadioData = DeezerDatastore.getInstance().getRadio(mCurRadioId, false);
                if (mRadioData != null) {
                    ImageHandler.getInstance().loadImageFromUrl(mCategoryCover,
                            mRadioData.getImageUrl(AImageOwner.ImageSize.big), true, R.drawable.ic_music_icon_grey);
                    mCategoryName.setText(mRadioData.getTitle());
                }
            }
        } else if (action.equals(Events.DEEZER.FETCH_RADIO_ERROR)) {
            long id = intent.getLongExtra(Events.DEEZER.Extra.RADIO_ID, -1);
            if (id == mCurRadioId) {
                final String errorMsg = I18n.tr("Couldn't get radio info");
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        } else if (action.equals(Events.UserFavorite.SET_FAVORITE_MUSIC_CHANNEL_COMPLETED)) {
            Tools.showToast(getActivity(), I18n.tr("Added to favorites"));
            mFavoriteIcon.setImageResource(R.drawable.ad_favourite_pink);

        } else if (action.equals(Events.UserFavorite.REMOVE_FAVORITE_MUSIC_CHANNEL_COMPLETED)) {
            Tools.showToast(getActivity(), I18n.tr("Removed from favorites"));
            mFavoriteIcon.setImageResource(R.drawable.ad_favourite_white);
        }
    }

    @Override
    public void onTrackEnded(Track track) {
    }

    @Override
    public void onRequestException(Exception e, Object o) {
    }

    private void requestRadioInfo() {
        DeezerDatastore.getInstance().getRadio(mCurRadioId, true);
        requestTrackList();
    }

    private void requestTrackList() {
        DeezerRequest request = DeezerRequestFactory.requestRadioTracks(mCurRadioId);
        request.setId("getRadioTrack");
        DeezerPlayerManager.getInstance().getDeezerConnect().requestAsync(request, new JsonRequestListener() {

            @Override
            public void onResult(Object response, Object requestId) {
                @SuppressWarnings("unchecked")
                List<Track> trackList = (List<Track>) response;
                updateTrackList(trackList);
            }

            @Override
            public void onUnparsedResult(String response, Object requestId) {
            }

            @Override
            public void onException(Exception error, Object requestId) {
                final String errorMsg = I18n.tr("Oops, couldn't get the track list");
                Logger.error.log(TAG, errorMsg);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPlayTrack(Track track) {
        if (mDeezerPlayback != null) {
            mDeezerPlayback.setTrackInfo(track);
            PlayerState playerState = DeezerPlayerManager.getInstance().getPlayerState();
            mDeezerPlayback.updateUIByPlayState(playerState);
            mTrackAdapter.setCurrentPlayTrack(track);
        }
    }

    private void updateTrackList(List<Track> trackList) {
        if (trackList != null) {
            mTrackAdapter.setTrackList(trackList);
        } else {
            mTrackAdapter.setTrackList(new ArrayList<Track>());
        }

        if (DeezerPlayerManager.getInstance().getBgPlayingTrack() != null) {
            mTrackAdapter.setCurrentPlayTrack(DeezerPlayerManager.getInstance().getBgPlayingTrack());
        }
    }

}
