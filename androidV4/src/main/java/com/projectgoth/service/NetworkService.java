/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.common.LoginConfig;
import com.projectgoth.blackhole.fusion.packet.FusionPktError;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.DefaultConfig;
import com.projectgoth.common.ErrorHandler;
import com.projectgoth.common.Theme;
import com.projectgoth.common.Version;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.NetworkResponseController;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.ConnectionService;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.utils.ConnectionConfig;
import com.projectgoth.receiver.NetworkServiceReceiver;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * NetworkService.java
 * 
 * @author warrenbalcos on Jun 12, 2013
 * 
 */
public class NetworkService extends Service {
    
    public static final String        EVENT_NETWORKSERVICE_STARTED = "NetworkService.EVENT_NETWORKSERVICE_STARTED";

    public static final String        EVENT_NETWORKSERVICE_STOPPED = "NetworkService.EVENT_NETWORKSERVICE_STOPPED";

    public static final String        EVENT_NETWORK_SERVICE_ALARM  = "NetworkService.EVENT_NETWORK_SERVICE_ALARM";

    private ConnectionServiceEx       serverConnectionService;

    private boolean                   wifiConnected;

    private boolean                   mobileConnected;

    private NetworkBroadcastReceiver  broadcastReceiver;

    private NetworkResponseController networkResponseController;

    private ErrorHandler              errorHandler;

    private static final int          ALARM_REQUEST_CODE           = 123123123;

    private final Binder              binder                       = new LocalBinder();

    public class LocalBinder extends Binder {

        public NetworkService getService() {
            return (NetworkService.this);
        }
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class ConnectionServiceEx extends ConnectionService {

        public ConnectionServiceEx() {
            super();
        }

        @Override
        public void onLoggedIn() {
            super.onLoggedIn();
            Session.getInstance().setLoginStatusLogin();
            Session.getInstance().storeLastLoginStatus(Session.STATUS_LOG_IN);
            resendAllFailedChatMessages();
            BroadcastHandler.Login.sendSuccess();
        }

        @Override
        public void onLoginProgress() {
            Session.getInstance().setLoginStatusLogging();
            BroadcastHandler.Login.sendProgress();
        }

        @Override
        public void onLoginError(String errorMessage, FusionPktError.ErrorType errorType) {
            Session.getInstance().setLoginStatusLogOut();
            Session.getInstance().storeLastLoginStatus(Session.STATUS_LOG_OUT);
            BroadcastHandler.Login.sendError(errorMessage, errorType);
        }

        @Override
        public void onServiceActivated() {
        }

        @Override
        public void onServiceDeactivated() {
            BroadcastHandler.Login.sendLogout();
        }

        @Override
        public void onConnected(){
            super.onConnected();
            Session.getInstance().setNetworkConnected(true);
            BroadcastHandler.NetworkService.sendStarted();
        }

        @Override
        public void onDisconnected() {
            super.onDisconnected();
            Session.getInstance().setNetworkConnected(false);
            BroadcastHandler.NetworkService.sendDisconnected();
            ChatDatastore.getInstance().resetAllChatSyncState();
            ChatDatastore.getInstance().disconnectAllChatrooms();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        // Initialize connection details
        ConnectionDetail connectionDetail = Config.getInstance().getConnectionDetail();

        serverConnectionService = new ConnectionServiceEx();
        initIsNetworkAvailable();
        broadcastReceiver = new NetworkBroadcastReceiver();

        serverConnectionService.setSession(Session.getInstance());

        if (Session.getInstance().isServiceActive()) {
            startServerConnectionService();
        }

        // Use the network controller to listen in on network responses.
        networkResponseController = new NetworkResponseController(this);
        serverConnectionService.setDefaultNetworkResponseListener(networkResponseController
                .getNetworkResponseListener());

        errorHandler = new ErrorHandler();
        serverConnectionService.setErrorEventListener(errorHandler.getErrorListener());

        ConnectionConfig config = serverConnectionService.getConfig();
        config.setMigmeApiUrl(connectionDetail.getMigmeApiUrl());
        config.setSignupServiceUrl(connectionDetail.getSignupServer());
        config.setMigboDataServiceUrl(UrlHandler.getInstance().getMigboDataServiceUrl());
        config.setSsoUrl(UrlHandler.getInstance().getSsoUrl());
        config.setMultiPartPostUrl(UrlHandler.getInstance().getMultiPartPostUrl());
        config.setImagesUrl(UrlHandler.getInstance().getImagesUrl());

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(AppEvents.Application.BACKGROUND_STATE_CHANGED));
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(AppEvents.Notification.UPDATE_AVAILABLE));
    }

    public void startServerConnectionService() {
        ConnectionDetail detail = Config.getInstance().getConnectionDetail();
        Proxy proxy = null;
        if (detail.isUseProxy()) {
            SocketAddress address = new InetSocketAddress(detail.getProxyHost(), detail.getProxyPort());
            proxy = new Proxy(Proxy.Type.HTTP, address);
        }
        
        serverConnectionService.setFusionServerConnection(detail.getGateway(), detail.getPort());
        serverConnectionService.setLoginConfig(createLoginConfig());
        serverConnectionService.setHttpProxy(proxy);
        serverConnectionService.startService();
        startServiceAlarm();
        
        // Re-initialize the ImageHandler. This will update Picasso with the current connection details
        ImageHandler.getInstance();
    }

    public boolean isLoggedIn(){
        if(serverConnectionService != null){
            return serverConnectionService.isLoggedIn();
        }
        return false;
    }

    public void logout() {
        serverConnectionService.stopService();
        Session.getInstance().setLoginStatusLogOut();
        Session.getInstance().storeLastLoginStatus(Session.STATUS_LOG_OUT);
        Session.getInstance().clearSession();
        AlertsDatastore.getInstance().clearData();
        if (!Config.isDebug()) {
            Session.getInstance().clearPasswordInLocal();
        }
    }

    public RequestManager getRequestManager() {
        if (serverConnectionService != null) {
            return serverConnectionService.getRequestManager();
        }
        return null;
    }

    public ConnectionConfig getConnectionConfig() {
        if (serverConnectionService != null) {
            return serverConnectionService.getConfig();
        }
        return null;
    }

    private void startServiceAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(EVENT_NETWORK_SERVICE_ALARM, null, this, NetworkServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ApplicationEx.getInstance(), ALARM_REQUEST_CODE,
                intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), Config.getInstance()
                .getServiceAlarmDelay(), pendingIntent);
    }

    /**
     * Network Broadcast Receiver
     * 
     * @author warrenbalcos Feb 1, 2012
     */
    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AppEvents.Application.BACKGROUND_STATE_CHANGED)) {
                serverConnectionService.setInBackground(ApplicationEx.getInstance().isApplicationInBackground());
            } else if (action.equals(AppEvents.Notification.UPDATE_AVAILABLE)) {
                if (ApplicationEx.getInstance().isApplicationInBackground()
                        || ApplicationEx.getInstance().isPhoneLocked()) {
                    // handle the notification the default way
                    ApplicationEx.getInstance().getNotificationHandler().showStatusNotification();
                }
            }
        }
    }

    private LoginConfig createLoginConfig() {

        LoginConfig loginConfig = new LoginConfig();

        // client type: 8=android
        loginConfig.setClientType(DefaultConfig.CLIENT_TYPE);
        loginConfig.setVersionNumber((short) Version.getVersionNumber());
        loginConfig.setUserAgent(Version.getUserAgent());

        loginConfig.setMobileDevice(Build.FINGERPRINT);
        loginConfig.setFontHeight(Config.getInstance().getRequestedEmoticonDimension());
        loginConfig.setScreenHeight(Config.getInstance().getScreenHeight());
        loginConfig.setScreenWidth(Config.getInstance().getScreenWidth());

        loginConfig.setLanguage(SystemDatastore.getInstance().getLanguage());

        int themeId = 1;
        try {
            themeId = Integer.parseInt(Theme.getId());
        } catch (NumberFormatException e) {
        }
        loginConfig.setThemeId(themeId);
        loginConfig.setPresence(Session.getInstance().getPresence());
        loginConfig.setFeedsDownload(Config.getInstance().getEnableFeedsDownload());

        // Get location information
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
            int cellID = location.getCid();
            if (cellID != -1) {
                loginConfig.setCellId(String.valueOf(cellID));
            }
            int lac = location.getLac();
            if (lac != -1) {
                loginConfig.setLocationAreaCode(String.valueOf(lac));
            }
        } catch (Exception e) {
            // do nothing
        }
        try {
            String networkOperator = tm.getNetworkOperator();
            int mcc = -1;
            int mnc = -1;
            if (networkOperator != null && networkOperator.length() > 3) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                mnc = Integer.parseInt(networkOperator.substring(3));
                loginConfig.setMobileCountryCode(String.valueOf(mcc));
                loginConfig.setMobileNetworkCode(String.valueOf(mnc));
            }
        } catch (Exception e) {
            // do nothing
        }

        // for application menu
        loginConfig.setAppMenuVersion(SystemDatastore.getInstance().getAppMenuVersion());
        loginConfig.setVasTrackingId(Version.getVasTrackingId());
        loginConfig.setPatch(Integer.parseInt(Version.getPatch()));
        
        loginConfig.setVgSize(ApplicationEx.getInstance().getResources().getDimensionPixelSize(R.dimen.vg_request_size));
        loginConfig.setStickerSize(ApplicationEx.getInstance().getResources().getDimensionPixelSize(R.dimen.sticker_request_size));

        return loginConfig;
    }

    /**
     * @return the wifiConnected
     */
    public boolean isWifiConnected() {
        return wifiConnected;
    }

    /**
     * @param wifiConnected
     *            the wifiConnected to set
     */
    public void setWifiConnected(boolean wifiConnected) {
        this.wifiConnected = wifiConnected;
    }

    /**
     * @return the mobileConnected
     */
    public boolean isMobileConnected() {
        return mobileConnected;
    }

    /**
     * @param mobileConnected
     *            the mobileConnected to set
     */
    public void setMobileConnected(boolean mobileConnected) {
        this.mobileConnected = mobileConnected;
    }

    public void updateNetworkStatus() {
        updateConnectionType();
        serverConnectionService.setNetworkAvailable(isNetworkAvailable());
    }

    public void initIsNetworkAvailable() {
        updateConnectionType();
        serverConnectionService.initIsNetworkAvailable(isNetworkAvailable());
    }

    private void updateConnectionType() {
        ConnectivityManager conn = (ConnectivityManager) getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            setWifiConnected(networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
            setMobileConnected(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
        } else {
            setWifiConnected(false);
            setMobileConnected(false);
        }
    }
    /**
     * Check if network connection is available
     * 
     * @return
     */
    public boolean isNetworkAvailable() {
        return isWifiConnected() || isMobileConnected();
    }

    /**
     * @return the proxy used by the connection service (if any)
     */
    public Proxy getProxy() {
        if (serverConnectionService != null) {
            return serverConnectionService.getHttpProxy();
        }
        return null;
    }

    public void stopServerConnectionService() {
        serverConnectionService.stopService();
    }

    private void resendAllFailedChatMessages() {
        ChatController.getInstance().resendAllFailedMessages();
    }

}
