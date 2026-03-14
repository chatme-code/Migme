
package com.projectgoth.music.deezer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.PlayerState;
import com.projectgoth.R;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.util.TimeUtils;
import java.util.concurrent.TimeUnit;

/**
 * Deezer playback UI
 * 
 * @author freddie.w
 */
public final class DeezerPlayback extends LinearLayout implements PlaybackHandler {

    private static final String        TAG                      = "DeezerPlayback";
    private static final int           MAX_PROGRESS_VALUE       = 1000;
    private TextView                   mCurrentPlayingTime;
    private TextView                   mDuration;
    private ProgressBar                mProgressBar;
    private ImageView                  mTrackCover;
    private TextView                   mTrackName;
    private TextView                   mArtistName;
    // Only support radio player, so it only has next button
    private ImageButton                mBtnStop;
    private ImageButton                mBtnPlayPause;
    private ImageButton                mBtnNext;
    private boolean                    mShowProgressBar;
    private DeezerPlayerActionListener mListener;
    private RotateAnimation            mRotateAnimation;
    private Context                    mContext;
    private static final int           INFINITY_ROTATION        = -1;
    private static final int           ROTATION_DURATION        = 900;
    private static final int           ROTATION_START_DEGREE    = 0;
    private static final int           ROTATION_STOP_DEGREE     = 360;
    private static final float         ROTATION_PIVOT_VALUE     = 0.5f;


    public interface DeezerPlayerActionListener {

        void onPlayerStop();
    }

    public DeezerPlayback(Context context) {
        super(context);
        this.mContext = context;
    }

    public DeezerPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupUI();
    }

    private void setupUI() {
        mCurrentPlayingTime = (TextView) findViewById(R.id.playingTime);
        mDuration = (TextView) findViewById(R.id.duration);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTrackCover = (ImageView) findViewById(R.id.trackCover);
        mTrackName = (TextView) findViewById(R.id.trackName);

        mArtistName = (TextView) findViewById(R.id.artistName);

        mBtnStop = (ImageButton) findViewById(R.id.playbackStop);
        mBtnStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onPlayerStop();
                }
            }
        });

        mBtnPlayPause = (ImageButton) findViewById(R.id.playbackPlayPause);
        mBtnPlayPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (DeezerPlayerManager.getInstance().getPlayerState() != PlayerState.WAITING_FOR_DATA) {
                    if (DeezerPlayerManager.getInstance().isPlaying()) {
                        DeezerPlayerManager.getInstance().pause();
                    } else {
                        DeezerPlayerManager.getInstance().play();
                    }
                }
            }
        });

        mBtnNext = (ImageButton) findViewById(R.id.playbackNext);
        mBtnNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                DeezerPlayerManager.getInstance().playNext();
            }
        });

        mDuration = (TextView) findViewById(R.id.duration);
        mCurrentPlayingTime = (TextView) findViewById(R.id.playingTime);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(MAX_PROGRESS_VALUE);
    }

    public void setTrackInfo(Track track) {
        if (track == null) {
            return;
        }

        Artist artist = track.getArtist();
        Album album = track.getAlbum();
        if (artist != null) {
            String artistName = artist.getName();
            String trackTitle = track.getTitle();
            if (!TextUtils.isEmpty(artistName)) {
                mArtistName.setText(artistName);
            }

            if (!TextUtils.isEmpty(trackTitle)) {
                mTrackName.setText(trackTitle);
            }
        }

        if (album != null) {
            final String coverUrl = album.getCoverUrl();
            if (!TextUtils.isEmpty(coverUrl)) {
                ImageHandler.getInstance().loadImageFromDeezerUrl(mTrackCover, coverUrl, true,
                        R.drawable.ic_default_cover_loading, -1, new ImageHandler.ImageLoadListener() {
                            @Override
                            public void onImageLoaded(Bitmap bitmap) {
                                NativeNotificationManager.getInstance().displayNotificationBar(bitmap);
                            }

                            @Override
                            public void onImageFailed(ImageView imageView) {
                                //fall through
                            }
                        });
            }
        }

        mDuration.setText(TimeUtils.durationToString(TimeUnit.MILLISECONDS.toSeconds(track.getDuration())));
    }

    public void showProgressBar(boolean isShow) {
        mShowProgressBar = isShow;
        LinearLayout progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);
        if (mShowProgressBar) {
            progressBarLayout.setVisibility(View.VISIBLE);
        } else {
            progressBarLayout.setVisibility(View.GONE);
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
        double percent = timePosition / (double) duration;
        mProgressBar.setProgress((int) (1000 * percent));
        mCurrentPlayingTime.setText(TimeUtils.durationToString(TimeUnit.MILLISECONDS.toSeconds(timePosition)));
        mDuration.setText(TimeUtils.durationToString(TimeUnit.MILLISECONDS.toSeconds(duration)));
    }

    @Override
    public void onPlayerStateChange(PlayerState playerState, long timePosition) {
        Log.d(TAG, "play state change, current: " + playerState.toString());
        updateUIByPlayState(playerState);
    }

    public void updateUIByPlayState(PlayerState playerState) {
        updatePlaybackControlUi(playerState);
        updateProgressBar(playerState);
    }

    private void updatePlaybackControlUi(final PlayerState playerState) {
        try {
            if (mContext != null) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(playerState);
                        if (playerState == PlayerState.PLAYING) {
                            if (mBtnPlayPause.getDrawable() != getResources().getDrawable(R.drawable.ic_action_pause_white_solid)) {
                                mBtnPlayPause.setImageResource(R.drawable.ic_action_pause_white_solid);
                            }
                        } else if (playerState == PlayerState.WAITING_FOR_DATA) {
                            if (mBtnPlayPause.getDrawable() != getResources().getDrawable(R.drawable.ad_loader_music_green)) {
                                mBtnPlayPause.setImageResource(R.drawable.ad_loader_music_green);
                            }
                        } else {
                            if (mBtnPlayPause.getDrawable() != getResources().getDrawable(R.drawable.ic_action_play_green)) {
                                mBtnPlayPause.setImageResource(R.drawable.ic_action_play_green);
                            }
                        }
                        mBtnPlayPause.requestLayout();
                    }
                });
            }
        } catch (Exception e) {
            Log.d(TAG, "play state set play button UI, current: " + playerState.toString() + ", Error is : " + e);
        }
    }

    private void updateProgressBar(PlayerState playerState) {
        if (playerState == PlayerState.STOPPED) {
            mProgressBar.setProgress(0);
            mCurrentPlayingTime.setText(TimeUtils.durationToString(TimeUnit.MILLISECONDS.toSeconds(0)));
        }
    }

    public void setDeezerPlayerActionListener(DeezerPlayerActionListener listener) {
        this.mListener = listener;
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
        if (mBtnPlayPause != null) {
            if (playerState == PlayerState.WAITING_FOR_DATA) {
                mBtnPlayPause.setAnimation(mRotateAnimation);
                mRotateAnimation.startNow();
            } else {
                mRotateAnimation.cancel();
            }
        }
    }
}