package com.projectgoth.music.deezer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.DeezerDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.listener.TrackListJsonRequestListener;


public final class DeezerPlaybackChat extends BaseDeezerPlayback implements DeezerPlaybackHandler {
    private static final String TAG = "DeezerPlaybackChat";

    private static final String         DEFAULT_TRACK_ID        = "999";

    private ImageView                   mTrackCover;
    private ImageView                   mPlayingCover;
    private TextView                    mRadioTitle;
    private TextView                    mTrackArtist;
    private TextView                    mTrackName;
    private ImageButton                 mBtnStartPause;
    private ImageButton                 mBtnNext;
    private ImageButton                 mBtnPlayStop;
    private HoloCircularProgressBar     mProgressbar;
    private DeezerRadio                 mRadio;
    private RotateAnimation             mRotateAnimation;
    private static final int            INFINITY_ROTATION       = -1;
    private static final int            ROTATION_DURATION       = 900;
    private static final int            ROTATION_START_DEGREE   = 0;
    private static final int            ROTATION_STOP_DEGREE    = 360;
    private static final float          ROTATION_PIVOT_VALUE    = 0.5f;


    public DeezerPlaybackChat(Context context) {
        super(context);
    }

    public DeezerPlaybackChat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setListener();
        setupUI();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DeezerPlayerManager playerManager = DeezerPlayerManager.getInstance();
        playerManager.removeAllListener();
        playerManager.clearRadioPlayerListener();
    }

    private void setListener() {
        DeezerPlayerManager playerManager = DeezerPlayerManager.getInstance();
        playerManager.removeAllListener();
        playerManager.resetRadioPlayerListener(this);
        playerManager.attachPlayer(this);
    }

    private void setupUI() {
        mProgressbar = (HoloCircularProgressBar) findViewById(R.id.progress_bar);
        mPlayingCover = (ImageView) findViewById(R.id.chat_playing_cover);
        mPlayingCover.setVisibility(View.INVISIBLE);
        mTrackCover = (ImageView) findViewById(R.id.album_cover);
        mRadioTitle = (TextView) findViewById(R.id.song_name);
        mTrackName = (TextView) findViewById(R.id.track_description);
        mTrackArtist = (TextView) findViewById(R.id.artist_name);
        mBtnStartPause = (ImageButton) findViewById(R.id.playAndPause);
        mBtnStartPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPlayerId = DeezerPlayerManager.getInstance().getCurrentPlayerId();
                long radioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();
                playSong(DEFAULT_TRACK_ID, currentPlayerId, radioId, false);
                updatePlaybackControlUI();
            }
        });

        mBtnNext = (ImageButton) findViewById(R.id.nextSong);
        mBtnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPlayerId = DeezerPlayerManager.getInstance().getCurrentPlayerId();
                long radioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();
                playSong(DEFAULT_TRACK_ID, currentPlayerId, radioId, true);
            }
        });
        mBtnPlayStop = (ImageButton) findViewById(R.id.previousSong);
        mBtnPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    public void playPinnedSong(long radioId) {
        if (DeezerPlayerManager.getInstance().isPlaying()) {
            playSong(DEFAULT_TRACK_ID, DeezerPlayerManager.getInstance().getCurrentPlayerId(), radioId, true);
        } else {
            setToDefaultData();
        }
    }

    public void setToDefaultData() {

        if (DeezerPlayerManager.getInstance().getBgPlayingRadioId() != DeezerPlayerManager.INVALID_RADIO_ID) {
            mTrackArtist.setText("");
            mTrackName.setText("");
            mTrackCover.setImageResource(R.drawable.ic_default_cover_loading);
            mRadio = DeezerDatastore.getInstance().getRadio(DeezerPlayerManager.getInstance().getBgPlayingRadioId(), false);
            if (mRadio != null) {
                mRadioTitle.setText(mRadio.getTitle());
                ImageHandler.getInstance().loadImageFromUrl(mTrackCover, mRadio.getImageUrl(AImageOwner.ImageSize.big), true, R.drawable.ic_default_cover_loading);
            }
        }
    }

    public void setTrackData() {

        setToDefaultData();
        // means radio id is set
        long bgPlayingRadioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();
        Track bgPlayingTrack = DeezerPlayerManager.getInstance().getBgPlayingTrack();

        if (bgPlayingRadioId != DeezerPlayerManager.INVALID_RADIO_ID) {
            if (bgPlayingTrack == null) {
                getTrackList(bgPlayingRadioId);
                updatePlaybackControlUI();
            } else {
                setTrackInfo(bgPlayingTrack);
                updatePlaybackControlUI();
            }

        }
    }

    private void playSong(String dataId, String currentPlayerId, long radioId, boolean isNext) {
        if (DeezerPlayerManager.getInstance().getPlayerState() != PlayerState.WAITING_FOR_DATA) {
            if (dataId.equals(currentPlayerId)) {
                //if this is current player pause or play
                if (DeezerPlayerManager.getInstance().isPlaying()) {
                    DeezerPlayerManager.getInstance().pause();
                    if (isNext) {
                        resetProgressbar();
                        DeezerPlayerManager.getInstance().playNext();
                    }
                } else {
                    DeezerPlayerManager.getInstance().playRadio(radioId);
                    if (isNext) {
                        resetProgressbar();
                        DeezerPlayerManager.getInstance().playNext();
                    }
                }
            } else {
                //set as current player
                setAsRadioPlayerListener();
                DeezerPlayerManager.getInstance().setCurrentPlayerId(dataId);
                DeezerPlayerManager.getInstance().playRadio(radioId);
            }
        }
    }

    private void setAsRadioPlayerListener() {
        DeezerPlayerManager.getInstance().removeAllListener();
        DeezerPlayerManager.getInstance().resetRadioPlayerListener(DeezerPlaybackChat.this);
        DeezerPlayerManager.getInstance().attachPlayer(DeezerPlaybackChat.this);
    }

    public void setTrackInfo(Track track) {
        if (track != null) {
            String radioName = "";
            String trackName = "";
            String artistName = "";

            mRadio = DeezerDatastore.getInstance().getRadio(DeezerPlayerManager.getInstance().getBgPlayingRadioId(), false);

            Artist artist = track.getArtist();
            Album album = track.getAlbum();
            trackName = track.getTitle();

            if (artist != null) {
                artistName = artist.getName();
            }

            if (mRadio == null) {
                //FIXME: What should we do if we can't get valid radio id
            } else {
                radioName = mRadio.getTitle();
            }

            if (!TextUtils.isEmpty(radioName)) {
                mRadioTitle.setText(radioName);
            }
            if (!TextUtils.isEmpty(trackName)) {
                mTrackName.setText(trackName);
            }
            if (!TextUtils.isEmpty(artistName)) {
                mTrackArtist.setText(artistName);
            }
            if (album != null) {
                final String coverUrl = album.getCoverUrl();
                if (!TextUtils.isEmpty(coverUrl)) {
                    ImageHandler.getInstance().loadImageFromUrl(mTrackCover, coverUrl, true, R.drawable.ad_loadstaticchat_grey);
                }
            }
        }
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
        updateUIByPlayState(playerState);

    }

    @Override
    public void onPlayerError(Exception e, long l) {

    }

    @Override
    public void onTooManySkipsException() {

    }

    @Override
    public void onAllTracksEnded() {

    }

    @Override
    public void onPlayTrack(Track track) {
        setTrackInfo(track);
    }

    @Override
    public void onTrackEnded(Track track) {

    }

    @Override
    public void onRequestException(Exception e, Object o) {

    }

    public void updateUIByPlayState(PlayerState playerState) {
        updatePlaybackControlUI(playerState);
    }

    private void updatePlaybackControlUI() {
        updatePlaybackControlUI(DeezerPlayerManager.getInstance().getPlayerState());
    }

    private void updatePlaybackControlUI(final PlayerState playerState) {
        try {
            showLoading(playerState);
            if (playerState == PlayerState.PLAYING) {
                if (mBtnStartPause.getDrawable() != getResources().getDrawable(R.drawable.ic_action_pause_white_solid_big)) {
                    mBtnStartPause.setImageResource(R.drawable.ic_action_pause_white_solid_big);
                }
            } else if (playerState == PlayerState.WAITING_FOR_DATA) {
                if (mBtnStartPause.getDrawable() != getResources().getDrawable(R.drawable.ad_loader_music_green)) {
                    mBtnStartPause.setImageResource(R.drawable.ad_loader_music_green);
                }
            } else {
                if (mBtnStartPause.getDrawable() != getResources().getDrawable(R.drawable.ic_action_play_grey_big)) {
                    mBtnStartPause.setImageResource(R.drawable.ic_action_play_grey_big);
                }
            }
            mBtnStartPause.requestLayout();

        } catch (Exception e) {
            Log.d(TAG, "play state set play button UI, current: " + playerState.toString() + ", Error is : " + e);
        }
    }

    private void resetProgressbar() {
        mProgressbar.setProgress(0);
        setPlayingCoverVisible(false);
    }

    public void setPlayingCoverVisible(boolean toShow) {
        if (toShow) {
            mPlayingCover.setVisibility(View.VISIBLE);
        } else {
            mPlayingCover.setVisibility(View.INVISIBLE);
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
        if (mBtnStartPause != null) {
            if (playerState == PlayerState.WAITING_FOR_DATA) {
                mBtnStartPause.setAnimation(mRotateAnimation);
                mRotateAnimation.startNow();
            } else {
                mRotateAnimation.cancel();
            }
        }
    }

    public void updateUI() {
        updatePlaybackControlUI(DeezerPlayerManager.getInstance().getPlayerState());
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
}
