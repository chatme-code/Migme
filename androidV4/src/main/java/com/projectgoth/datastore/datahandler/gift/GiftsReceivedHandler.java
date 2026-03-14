package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.UserGiftListData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetUserGiftListListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * Created by mapet on 3/6/15.
 */
public class GiftsReceivedHandler extends DataHandler<UserGiftListData> {

    private static final String KEY_PREFIX = "GR";

    private static final String PARAM_USERID = "PARAM_USERID";
    private static final String PARAM_CATEGORY = "PARAM_CATEGORY";
    private static final String PARAM_MONTH = "PARAM_MONTH";
    private static final String PARAM_YEAR = "PARAM_YEAR";
    private static final String PARAM_ORDERBY = "PARAM_ORDERBY";
    private static final String PARAM_ASCDESC = "PARAM_ASCDESC";
    private static final String PARAM_LIMIT = "PARAM_LIMIT";
    private static final String PARAM_OFFSET = "PARAM_OFFSET";

    private ServerHandler<UserGiftListData> mServerHandler = new ServerHandler<UserGiftListData>() {

        @Override
        public void doFetch(final Bundle params, final ServerResult<UserGiftListData> callback) {
            final String userId = params.getString(PARAM_USERID);
            final String category = params.getString(PARAM_CATEGORY);
            final String month = params.getString(PARAM_MONTH);
            final String year = params.getString(PARAM_YEAR);
            final String orderType = params.getString(PARAM_ORDERBY);
            final String sortOrder = params.getString(PARAM_ASCDESC);
            final int offset = params.getInt(PARAM_OFFSET);
            final int limit = params.getInt(PARAM_LIMIT);

            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getUserGiftList(new GetUserGiftListListener() {
                    @Override
                    public void onUserGiftListReceived(UserGiftListData data) {
                        broadcastSuccessEvent(data);
                        callback.onSuccess(params, data);
                    }
                }, userId, category, month, year, String.valueOf(offset), String.valueOf(limit), orderType, sortOrder);
            }
        }
    };

    public GiftsReceivedHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public UserGiftListData getData(String userId, GiftsDatastore.Category category, String month, String year,
                                    GiftsDatastore.OrderType orderType, GiftsDatastore.SortOrder sortOrder,
                                    int offset, int limit, boolean forceFetch) {
        Bundle params = createBundle(userId, category, month, year, orderType, sortOrder, offset, limit);
        return getData(params, forceFetch);
    }

    private Bundle createBundle(String userId, GiftsDatastore.Category category, String month, String year,
                                GiftsDatastore.OrderType orderType, GiftsDatastore.SortOrder sortOrder,
                                int offset, int limit) {
        Bundle result = new Bundle();
        result.putString(PARAM_USERID, userId);
        result.putString(PARAM_CATEGORY, category.toString());
        result.putString(PARAM_MONTH, month);
        result.putString(PARAM_YEAR, year);
        result.putString(PARAM_ORDERBY, orderType.toString());
        result.putString(PARAM_ASCDESC, sortOrder.toString());
        result.putInt(PARAM_OFFSET, offset);
        result.putInt(PARAM_LIMIT, limit);
        return result;
    }

    @Override
    public void broadcastSuccessEvent(UserGiftListData data) {
        BroadcastHandler.Gift.sendFetchGiftListReceivedCompleted();
    }

    @Override
    public void broadcastFailEvent(MigError error) {
    }

    @Override
    public String getKey(Bundle params) {
        return Tools.createKey(KEY_PREFIX, Constants.MINUSSTR, params.getString(PARAM_USERID),
                params.getString(PARAM_CATEGORY), params.getString(PARAM_ORDERBY), params.getString(PARAM_ASCDESC));
    }
}
