package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.TextUtils;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.music.deezer.PlaybackHandler;
import com.projectgoth.ui.holder.content.DeezerContentViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;

/**
 * Created by houdangui on 16/3/15.
 */
public class DeezerContentViewAction extends ContentViewAction<DeezerContentViewHolder> implements RadioPlayerListener, OnPlayerErrorListener, ContextMenuItemListener{
    /**
     * Constructor.
     *
     * @param contentViewHolder The {@link com.projectgoth.ui.holder.content.DeezerContentViewHolder} for which actions will need to be handled.
     */
    protected ImageView                mTrackCover;
    protected ImageView                deezerBanner;
    protected ImageButton              mBtnNext;
    protected ImageButton              mBtnPlayPause;
    protected ImageButton              mBtnPlayStop;
    protected TextView                 mRadioTitle;
    protected TextView                 mArtistName;
    protected TextView                 mTrackName;
    protected DeezerContentViewHolder  contentViewHolder;
    protected LinearLayout             mPlaybackBody;
    protected FragmentActivity         activity;


    public DeezerContentViewAction(DeezerContentViewHolder contentViewHolder) {
        super(contentViewHolder);
        this.contentViewHolder = contentViewHolder;
    }

    @Override
    public void applyToView() {
        Logger.debug.log("deezerIssue", "action:" + this);
        mPlaybackBody = (LinearLayout) contentViewHolder.getContentView().findViewById(R.id.inline_playback_body);
        mRadioTitle = (TextView) contentViewHolder.getContentView().findViewById(R.id.inline_radio_title);
        mArtistName = (TextView) contentViewHolder.getContentView().findViewById(R.id.inline_artist_name);
        mTrackName = (TextView) contentViewHolder.getContentView().findViewById(R.id.inline_track_name);
        mTrackCover = (ImageView) contentViewHolder.getContentView().findViewById(R.id.inline_album_cover);
        mBtnPlayPause = (ImageButton) contentViewHolder.getContentView().findViewById(R.id.inline_playAndPause);
        mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });
        mBtnNext = (ImageButton) contentViewHolder.getContentView().findViewById(R.id.inline_next_song);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });
        resetRadioPlayerListenerIfNeeded();

//        mBtnPlayStop = (ImageButton) contentViewHolder.getContentView().findViewById(R.id.inline_previous_song);
//        mBtnPlayStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                RadioPlayerEx radioPlayer = ApplicationEx.getInstance().getDeezerPlayer();
//                if (radioPlayer == null) {
//                    return;
//                }
//                if (radioPlayer.getPlayerState() == PlayerState.PLAYING) {
//                    radioPlayer.stop();
//                }
//            }
//        });
    }

    /**
     * reset listener of the player since when message list refreshed, the DeezerContentViewAction will be recreated
     */
    private void resetRadioPlayerListenerIfNeeded() {
        String dataId = contentViewHolder.getMimeData().getDataId();
        String currentPlayerId = DeezerPlayerManager.getInstance().getCurrentPlayerId();
        if (currentPlayerId == null) {
            currentPlayerId = "";
        }
        if (dataId == null) {
            dataId = "";
        }
        if (dataId.equals(currentPlayerId)) {
            setAsRadioPlayerListener();
        }
    }

    private void setAsRadioPlayerListener() {
        DeezerPlayerManager.getInstance().removeAllListener();
        DeezerPlayerManager.getInstance().resetRadioPlayerListener(DeezerContentViewAction.this);
        DeezerPlayerManager.getInstance().attachPlayer(DeezerContentViewAction.this);
    }

    private void playSong() {
        String dataId = contentViewHolder.getMimeData().getDataId();
        String currentPlayerId = DeezerPlayerManager.getInstance().getCurrentPlayerId();
        long radioId = contentViewHolder.getMimeData().getLongId();
        playSong(dataId, currentPlayerId, radioId, false);
    }

    private void playNextSong() {
        String dataId = contentViewHolder.getMimeData().getDataId();
        String currentPlayerId = DeezerPlayerManager.getInstance().getCurrentPlayerId();
        long radioId = contentViewHolder.getMimeData().getLongId();
        playSong(dataId, currentPlayerId, radioId, true);
    }

    private void playSong(String dataId, String currentPlayerId, long radioId, boolean isNext) {
        if (DeezerPlayerManager.getInstance().getPlayerState() != PlayerState.WAITING_FOR_DATA) {
            if (dataId.equals(currentPlayerId)) {
                //if this is current player pause or play
                if (DeezerPlayerManager.getInstance().isPlaying()) {
                    if (isNext) {
                        DeezerPlayerManager.getInstance().playNext();
                    } else {
                        DeezerPlayerManager.getInstance().pause();
                    }
                } else {
                    if (isNext) {
                        DeezerPlayerManager.getInstance().playNext();
                    } else {
                        DeezerPlayerManager.getInstance().playRadio(radioId);
                    }
                }
            } else {
                //start play
                if (!isNext) {
                    //set as current player
                    setAsRadioPlayerListener();
                    DeezerPlayerManager.getInstance().setCurrentPlayerId(dataId);
                    //start play

                    DeezerPlayerManager.getInstance().playRadio(radioId);
                }
            }
        }
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
        contentViewHolder.setTrackInfo(track);
    }

    @Override
    public void onTrackEnded(Track track) {

    }

    @Override
    public void onRequestException(Exception e, Object o) {

    }

    public PlaybackHandler getPlayBackHandler() {
        return contentViewHolder;
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);
        switch (parameter) {
            case ACTIVITY:
                this.activity = (FragmentActivity) value;
                break;
            default:
                break;
        }
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_share:
                String url = Constants.BLANKSTR;
                if (contentViewHolder.getRadio().getShareUrl() != null) {
                    url = contentViewHolder.getRadio().getShareUrl();
                }
                String strRadioId = String.valueOf(contentViewHolder.getRadio().getId());
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(strRadioId)) {
                    ShareManager.shareDeezerRadio(activity, String.valueOf(contentViewHolder.getRadio().getId()), url);
                }
                break;
            case R.id.option_item_pin:

                break;
            default:
                break;
        }
    }
}
