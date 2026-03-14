package com.projectgoth.datastore;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.RequestListener;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.music.deezer.DeezerRadio;

/**
 * Created by houdangui on 19/3/15.
 */
public class DeezerDatastore extends BaseDatastore {

    private static final Object CACHE_LOCK = new Object();

    private HashMap<String, DeezerRadio> mRadiosCache;

    private static final int RADIO_MAX_CACHE_SIZE = 20;

    private DeezerDatastore() {
        super();
    }

    private static class DeezerDatastoreHolder {
        static final DeezerDatastore sINSTANCE = new DeezerDatastore();
    }

    /**
     * A singleton point of access for this class.
     *
     * @return An instance of DeezerDatastore.
     */
    public static DeezerDatastore getInstance() {
        return DeezerDatastoreHolder.sINSTANCE;
    }

    @Override
    protected void initData() {
        synchronized (CACHE_LOCK) {
            mRadiosCache = new HashMap<String, DeezerRadio>();
        }
    }

    public void cacheRadio(DeezerRadio radio) {
        synchronized (CACHE_LOCK) {
            mRadiosCache.put(String.valueOf(radio.getId()), radio);
        }
    }

    public DeezerRadio getRadio(long radioId, boolean shouldForceFetch) {
        synchronized (CACHE_LOCK) {
            if (radioId < 0) {
                //in case it is invalid id
                return null;
            }
            String key = String.valueOf(radioId);
            if (mRadiosCache.get(key) == null || shouldForceFetch) {
                BroadcastHandler.Deezer.sendFetchRadioBegin(radioId);
                requestRadioInfo(radioId);
            }
            return mRadiosCache.get(key);
        }
    }

    private void requestRadioInfo(final long radioId) {
        DeezerRequest request = DeezerRequestFactory.requestRadio(radioId);
        request.setId("getRadio");
        DeezerPlayerManager.getInstance().getDeezerConnect().requestAsync(request, new RequestListener() {
            @Override
            public void onComplete(String response, Object requestId) {
                try {
                    //cache radio
                    DeezerRadio radioData = new DeezerRadio(new JSONObject(response));
                    cacheRadio(radioData);
                    //send broadcast
                    BroadcastHandler.Deezer.sendRadioReceived(radioId);
                } catch (JSONException e) {
                    onException(e, requestId);
                }
            }

            @Override
            public void onException(Exception e, Object requestId) {
                BroadcastHandler.Deezer.sendFetchRadioError(radioId);
            }
        });

    }

}
