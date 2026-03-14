package com.projectgoth.ui.listener;

import android.util.Log;
import android.widget.Toast;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.i18n.I18n;
import com.projectgoth.music.deezer.DeezerPlaybackHandler;

import java.util.List;

/**
 * Created by justinhsu on 4/13/15.
 */
public class TrackListJsonRequestListener extends JsonRequestListener {
    private static final String TAG = "TrackListJsonRequestListener";
    DeezerPlaybackHandler mBaseDeezerInterface;

    public TrackListJsonRequestListener(DeezerPlaybackHandler baseDeezerInterface) {
        this.mBaseDeezerInterface = baseDeezerInterface;
    }

    @Override
    public void onResult(Object response, Object requestId) {
        List<Track> trackList = (List<Track>) response;
        mBaseDeezerInterface.setTrackInfo(trackList.get(0));
    }

    @Override
    public void onUnparsedResult(String response, Object requestId) {
    }

    @Override
    public void onException(Exception error, Object requestId) {
        final String errorMsg = "Oops, couldn't get the track list";
        Log.e(TAG, I18n.tr(errorMsg));
        Toast.makeText(ApplicationEx.getInstance().getCurrentActivity(), I18n.tr(errorMsg), Toast.LENGTH_SHORT).show();
    }

}
