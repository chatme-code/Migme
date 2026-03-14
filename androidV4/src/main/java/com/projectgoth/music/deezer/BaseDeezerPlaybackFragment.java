
package com.projectgoth.music.deezer;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.fragment.BaseSearchFragment;
import com.projectgoth.util.AndroidLogger;

public abstract class BaseDeezerPlaybackFragment extends BaseSearchFragment implements RadioPlayerListener,
        OnPlayerErrorListener {

    private static final String TAG = AndroidLogger.makeLogTag(BaseDeezerPlaybackFragment.class);

    protected DeezerPlayback    mDeezerPlayback;

    protected Toast             mErrorToast;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeezerPlayback = getPlayback();
    }

    @Override
    public void onResume() {
        super.onResume();
        DeezerPlayerManager.getInstance().addListener(this);
        DeezerPlayerManager.getInstance().attachPlayer(mDeezerPlayback);
        PlayerState playerState = DeezerPlayerManager.getInstance().getPlayerState();
        if (playerState == PlayerState.STOPPED || playerState == PlayerState.RELEASED) {
            long radioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();
            DeezerPlayerManager.getInstance().playRadio(radioId);
        } else {
            Track track = DeezerPlayerManager.getInstance().getBgPlayingTrack();
            mDeezerPlayback.setTrackInfo(track);
            mDeezerPlayback.updateUIByPlayState(playerState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        DeezerPlayerManager.getInstance().removeListener(this);
        if (mDeezerPlayback != null) {
            DeezerPlayerManager.getInstance().detachPlayer(mDeezerPlayback);
        }
    }

    protected DeezerPlayback getPlayback() {
        if (mDeezerPlayback == null) {
            mDeezerPlayback = (DeezerPlayback) getView().findViewById(R.id.deezer_playback);
        }
        return mDeezerPlayback;
    }

    @Override
    public void onPlayerError(Exception error, long timePosition) {
        handleError(error);
    }

    protected void handleError(final Exception exception) {

        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    String message = exception.getMessage();
                    if (TextUtils.isEmpty(message)) {
                        message = exception.getClass().getName();
                    }

                    showError(message);
                }
            });
        }

        Log.e(TAG, "Exception occured " + exception.getClass().getName(), exception);
    }

    @Override
    public void onTooManySkipsException() {
        final String message = "Skip too many tracks";
        Log.e(TAG, message);
        showError(message);
    }

    @Override
    public void onAllTracksEnded() {
        if (mDeezerPlayback != null) {
            DeezerPlayerManager.getInstance().detachPlayer(mDeezerPlayback);
        }
        DeezerPlayerManager.getInstance().release();
    }

    protected void showError(String message) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.w(TAG, "not attach an activity, do not show toast");
            return;
        }

        if (mErrorToast != null) {
            mErrorToast.cancel();
        }

        mErrorToast = Toast.makeText(activity, I18n.tr(message), Toast.LENGTH_SHORT);
        mErrorToast.show();
    }
}
