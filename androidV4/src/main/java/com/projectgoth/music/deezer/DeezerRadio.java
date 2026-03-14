package com.projectgoth.music.deezer;

import com.deezer.sdk.model.Radio;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extends {@link com.deezer.sdk.model.Radio} to include share url and track list
 * @author freddie.w
 */
public class DeezerRadio extends Radio {
    private final String mShareUrl;
    private final String mTrackListUrl;

    public DeezerRadio(JSONObject json) throws JSONException {
        super(json);
        mShareUrl = json.optString("share");
        mTrackListUrl = json.optString("tracklist");
    }

    public final String getShareUrl() {
        return mShareUrl;
    }

    public final String getTrackListUrl() {
        return mTrackListUrl;
    }

}
