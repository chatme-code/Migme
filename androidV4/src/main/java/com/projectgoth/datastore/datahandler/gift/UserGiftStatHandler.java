package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.UserGiftStatData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetUserGiftStatListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * Created by mapet on 21/5/15.
 */
public class UserGiftStatHandler extends DataHandler<UserGiftStatData> {

    private static final String KEY_PREFIX = "UGS";
    private static final String PARAM_YEAR = "PARAM_YEAR";
    private static final String PARAM_USERID = "PARAM_USERID";

    //@formatter:off
    private ServerHandler<UserGiftStatData> mServerHandler = new ServerHandler<UserGiftStatData>() {

        @Override
        public void doFetch(final Bundle params, final ServerResult<UserGiftStatData> callback) {
            final String userId = params.getString(PARAM_USERID);
            final String year = params.getString(PARAM_YEAR);

            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getGiftStatisticsByDate(new GetUserGiftStatListener() {
                    @Override
                    public void onUserGiftStatReceived(UserGiftStatData data) {
                        broadcastSuccessEvent(data);

                        if (data != null) {
                            callback.onSuccess(params, data);
                        }
                    }
                }, userId, year);
            }
        }
    };
    //@formatter:on

    public UserGiftStatHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public UserGiftStatData getData(String userId, String year, boolean forceFetch) {
        Bundle params = createBundle(userId, year);
        return getData(params, forceFetch);
    }

    private Bundle createBundle(String userId, String year) {
        Bundle result = new Bundle();
        result.putString(PARAM_USERID, userId);
        result.putString(PARAM_YEAR, year);
        return result;
    }

    @Override
    public void broadcastSuccessEvent(UserGiftStatData data) {
        BroadcastHandler.Gift.sendFetchGiftStatsByDateCompleted();
    }

    @Override
    public void broadcastFailEvent(MigError error) {
    }

    @Override
    public String getKey(Bundle params) {
        return Tools.createKey(KEY_PREFIX, Constants.MINUSSTR, params.getString(PARAM_USERID));
    }
}
