package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.PlayerState;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;
import com.projectgoth.R;
import com.projectgoth.b.data.mime.DeezerMimeData;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.DeezerDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.music.deezer.DeezerPlaybackHandler;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.music.deezer.DeezerRadio;
import com.projectgoth.music.deezer.PlaybackHandler;
import com.projectgoth.ui.listener.TrackListJsonRequestListener;

/**
 * Created by houdangui on 16/3/15.
 */
public class DeezerContentViewHolder extends ContentViewHolder<DeezerMimeData, RelativeLayout> implements PlaybackHandler, DeezerPlaybackHandler {

    protected ImageView                     mPlayingCover;
    protected ImageView                     mTrackCover;
    protected ImageView                     mDeezerBanner;
    protected ImageButton                   mNextSong;
    protected ImageButton                   mPreviousSong;
    protected ImageButton                   mPlayOrPause;
    protected TextView                      mRadioTitle;
    protected TextView                      mTrackName;
    protected TextView                      mArtistName;
    protected Context                       mContext;
    protected HoloCircularProgressBar       mProgressbar;
    protected DeezerRadio                   mRadio;
    protected LinearLayout                  mPlaybackBody;
    private static final String             TAG                     = "DeezerContentViewHolder";
    protected DeezerMimeData                mDeezerMimeData;
    private RotateAnimation                 mRotateAnimation;
    private static final int                INFINITY_ROTATION       = -1;
    private static final int                ROTATION_DURATION       = 900;
    private static final int                ROTATION_START_DEGREE   = 0;
    private static final int                ROTATION_STOP_DEGREE    = 360;
    private static final float              ROTATION_PIVOT_VALUE    = 0.5f;

    /**
     * Constructor.
     *
     * @param ctx      The {@link android.content.Context} to be used for inflation.
     * @param mimeData The {@link com.projectgoth.b.data.mime.DeezerMimeData} to be used as data for this holder.
     */
    public DeezerContentViewHolder(Context ctx, DeezerMimeData mimeData) {
        super(ctx, mimeData);
        mContext = ctx;
    }

    @Override
    public int getLayoutId() {
        DeezerMimeData data = getMimeData();
        DeezerMimeData.DeezerDataType dataType = data.getDataType();
        int layout = R.layout.deezer_playback_inline;
        switch (dataType) {
            case RADIO:
                layout = R.layout.deezer_playback_inline;
                break;
        }
        return layout;
    }

    @Override
    protected void initializeView() {
        mPlaybackBody = (LinearLayout) view.findViewById(R.id.inline_playback_body);
        mPlayingCover = (ImageView) view.findViewById(R.id.inline_playing_cover);
        mPlayingCover.setVisibility(View.INVISIBLE);
        mTrackCover = (ImageView) view.findViewById(R.id.inline_album_cover);
        mDeezerBanner = (ImageView) view.findViewById(R.id.deezerBanner);
        mNextSong = (ImageButton) view.findViewById(R.id.inline_next_song);
        mPlayOrPause = (ImageButton) view.findViewById(R.id.inline_playAndPause);
        mPreviousSong = (ImageButton) view.findViewById(R.id.inline_previous_song);
        mRadioTitle = (TextView) view.findViewById(R.id.inline_radio_title);
        mArtistName = (TextView) view.findViewById(R.id.inline_artist_name);
        mTrackName = (TextView) view.findViewById(R.id.inline_track_name);
        mProgressbar = (HoloCircularProgressBar) view.findViewById(R.id.inline_progressbar);
    }

    @Override
    public boolean applyMimeData() {
        boolean ret = false;
        mDeezerMimeData = getMimeData();
        DeezerMimeData.DeezerDataType dataType = mDeezerMimeData.getDataType();
        switch (dataType) {
            case RADIO:
                // get the Radio from cache, if no fetch it
                // setup UI
                long id = mDeezerMimeData.getLongId();
                mRadio = DeezerDatastore.getInstance().getRadio(id, false);
                if (mDeezerMimeData.getArtistName() == null) {
                    if (mRadio != null) {
                        mDeezerMimeData.setRadioTitle(mRadio.getTitle());
                        mRadioTitle.setText(mRadio.getTitle());
                        getTrackList(id);
                        ImageHandler.getInstance().loadImageFromUrl(mTrackCover, mRadio.getImageUrl(AImageOwner.ImageSize.big), true, R.drawable.ic_default_cover_loading);
                    }
                } else {
                    if (!TextUtils.isEmpty(mDeezerMimeData.getTrackName())) {
                        mTrackName.setText(mDeezerMimeData.getTrackName());
                    }
                    if (!TextUtils.isEmpty(mDeezerMimeData.getArtistName())) {
                        mArtistName.setText(mDeezerMimeData.getArtistName());
                    }
                    if (!TextUtils.isEmpty(mDeezerMimeData.getRadioTitle())) {
                        mRadioTitle.setText(mDeezerMimeData.getRadioTitle());
                    }
                    if (!TextUtils.isEmpty(mDeezerMimeData.getCover())) {
                        ImageHandler.getInstance().loadImageFromUrl(mTrackCover, mDeezerMimeData.getCover(), true, R.drawable.ic_default_cover_loading);
                    }
                }
                //set the button state
                if (DeezerPlayerManager.getInstance().getCurrentPlayerId().equals(mimeData.getDataId())) {
                    if (DeezerPlayerManager.getInstance().isPlaying()) {
                        mPlayOrPause.setImageResource(R.drawable.ic_action_pause_green_solid);
                    }
                }
                ret = true;
                break;
        }

        return ret;
    }

    public void setTrackInfo(Track track) {
        if (track != null) {
            String trackName = null;
            String artistName = null;
            Artist artist = track.getArtist();
            Album album = track.getAlbum();
            trackName = track.getTitle();
            if (artist != null) {
                artistName = artist.getName();
            }
            if (!TextUtils.isEmpty(trackName)) {
                this.mTrackName.setText(trackName);
                mDeezerMimeData.setTrackName(trackName);
            }
            if (!TextUtils.isEmpty(artistName)) {
                this.mArtistName.setText(artistName);
                mDeezerMimeData.setArtistName(artistName);
            }

            if (album != null) {
                final String coverUrl = album.getCoverUrl();
                if (!TextUtils.isEmpty(coverUrl)) {
                    mDeezerMimeData.setCover(coverUrl);
                    ImageHandler.getInstance().loadImageFromUrl(mTrackCover, coverUrl, true, R.drawable.ic_default_cover_loading);
                }
            }

        }
    }

    public DeezerRadio getRadio() {
        return mRadio;
    }

    public void setRadio(DeezerRadio radio) {
        this.mRadio = radio;
    }

    @Override
    public void onBufferError(Exception error, double percent) {
    }

    @Override
    public void onBufferProgress(double percent) {
        Log.d(TAG, "buffering audio, progress:" + percent);
    }

    @Override
    public void onBufferStateChange(BufferState bufferState, double percent) {
    }

    @Override
    public void onPlayerProgress(long timePosition) {
        long duration = DeezerPlayerManager.getInstance().getTrackDuration();
        final double percent = timePosition / (double) duration;
        setProgress(percent);
    }

    public void setProgress(double percent) {
        if (percent > 0) {
            setPlayingCoverVisible(true);
            mProgressbar.setProgress((float) percent);
        } else {
            setPlayingCoverVisible(false);
            mProgressbar.setProgress(0);
        }
    }

    @Override
    public void onPlayerStateChange(PlayerState playerState, long timePosition) {
        updatePlaybackControlUi(playerState);
    }

    public void resetProgressbar() {
        setProgress(0);
    }

    public void setPlayingCoverVisible(boolean toShow) {
        if (toShow) {
            mPlayingCover.setVisibility(View.VISIBLE);
        } else {
            mPlayingCover.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setParameter(Parameter parameter, Object value) {
        super.setParameter(parameter, value);

        if (parameter == Parameter.SHOW_INLINE_PLAY_BUTTONS) {
            Boolean showInlinePlayButtons = (Boolean) value;
            if (!showInlinePlayButtons.booleanValue()) {
                mNextSong.setVisibility(View.GONE);
                mPlayOrPause.setVisibility(View.GONE);
                mPreviousSong.setVisibility(View.GONE);
            }
        }
    }

    public void getTrackList(long radioId) {
        requestTrackList(radioId);
    }

    public void requestTrackList(final long radioId) {
        DeezerRequest request = DeezerRequestFactory.requestRadioTracks(radioId);
        request.setId("getRadioTrackList");
        DeezerPlayerManager.getInstance().getDeezerConnect()
                .requestAsync(request, new TrackListJsonRequestListener(this));
    }

    private void updatePlaybackControlUi(final PlayerState playerState) {
        try {
            showLoading(playerState);
            if (playerState == PlayerState.PLAYING) {
                if (mPlayOrPause.getDrawable() != mContext.getResources().getDrawable(R.drawable.ic_action_pause_green_solid)) {
                    mPlayOrPause.setImageResource(R.drawable.ic_action_pause_green_solid);
                }
            } else if (playerState == PlayerState.WAITING_FOR_DATA) {
                if (mPlayOrPause.getDrawable() != mContext.getResources().getDrawable(R.drawable.ad_loader_music_green)) {
                    mPlayOrPause.setImageResource(R.drawable.ad_loader_music_green);
                }
            } else {
                if (mPlayOrPause.getDrawable() != mContext.getResources().getDrawable(R.drawable.ic_action_play_green)) {
                    mPlayOrPause.setImageResource(R.drawable.ic_action_play_green);
                }
            }
            mPlayOrPause.requestLayout();
        } catch (Exception e) {
            Log.d(TAG, "play state set play button UI, current: " + playerState.toString() + ", Error is : " + e);
        }
    }

    private void initialLoading() {
        mRotateAnimation = new RotateAnimation(ROTATION_START_DEGREE, ROTATION_STOP_DEGREE, Animation.RELATIVE_TO_SELF, ROTATION_PIVOT_VALUE, Animation.RELATIVE_TO_SELF, ROTATION_PIVOT_VALUE);
        mRotateAnimation.setRepeatCount(INFINITY_ROTATION);
        mRotateAnimation.setDuration(ROTATION_DURATION);
    }

    private void showLoading(PlayerState playerState) {
        if (mRotateAnimation == null) {
            initialLoading();
        }
        if (mPlayOrPause != null) {
            if (playerState == PlayerState.WAITING_FOR_DATA) {
                mPlayOrPause.setAnimation(mRotateAnimation);
                mRotateAnimation.startNow();
            } else {
                mRotateAnimation.cancel();
            }
        }
    }
}
