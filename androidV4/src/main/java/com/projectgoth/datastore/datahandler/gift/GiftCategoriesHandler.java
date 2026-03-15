package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftCategoryData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetGiftCategoriesListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * Created by mapet on 19/5/15.
 */
public class GiftCategoriesHandler extends DataHandler<GiftCategoryData> {

    private static final String KEY_PREFIX = "GC";
    private static final String PARAM_USERID = "PARAM_USERID";

    //@formatter:off
    private ServerHandler<GiftCategoryData> mServerHandler = new ServerHandler<GiftCategoryData>() {

        @Override
        public void doFetch(final Bundle params, final ServerResult<GiftCategoryData> callback) {
            final String userId = params.getString(PARAM_USERID);

            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getGiftCategories(new GetGiftCategoriesListener() {
                    @Override
                    public void onGiftCategoriesReceived(GiftCategoryData data) {
                        broadcastSuccessEvent(data);
                        callback.onSuccess(params, data);
                    }
                }, userId);
            }
        }
    };
    //@formatter:on

    public GiftCategoriesHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public GiftCategoryData getData(String userId, boolean forceFetch) {
        Bundle params = createBundle(userId);
        return getData(params, forceFetch);
    }

    private Bundle createBundle(String userId) {
        Bundle result = new Bundle();
        result.putString(PARAM_USERID, userId);
        return result;
    }

    @Override
    public void broadcastSuccessEvent(GiftCategoryData data) {
        BroadcastHandler.Gift.sendFetchGiftCategoriesCompleted();
    }

    @Override
    public void broadcastFailEvent(MigError error) {
    }

    @Override
    public String getKey(Bundle params) {
        return Tools.createKey(KEY_PREFIX, Constants.MINUSSTR, params.getString(PARAM_USERID));
    }
}
