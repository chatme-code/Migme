/**
 * 
 */
package com.projectgoth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.service.NetworkService;

/**
 * @author Warren Balcos Mig33 Aug 4, 2011
 * 
 */
public class NetworkServiceReceiver extends BroadcastReceiver {
	
    private static final String     LOG_TAG = AndroidLogger.makeLogTag(NetworkServiceReceiver.class);

	private static final boolean	SHOW_CONNECTION_TOAST	= false;
	
	private ApplicationEx			appEx;
	private NetworkService			service;
	
	public void onReceive(Context context, Intent intent) {
		appEx = ApplicationEx.getInstance();
		if (appEx != null) {
			service = appEx.getNetworkService();
		}
		
		String action = intent.getAction();
		Logger.debug.log(LOG_TAG, "NetworkServiceReceiver recieved: ", action);
		if (action != null) {
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (null != service) {
					service.updateNetworkStatus();

                    BroadcastHandler.NetworkService.sendNetworkStatusChanged();
					
					if (SHOW_CONNECTION_TOAST) {
						if (service.isWifiConnected()) {
						    Tools.showToast(context, I18n.tr("Connected to WiFi"));
						} else if (service.isMobileConnected()) {
							Tools.showToast(context, I18n.tr("Connected to mobile network"));
						} else {
							Tools.showToast(context, I18n.tr("No internet connection"));
						}
					}
					
					if (Session.getInstance().isServiceActive() && service.isNetworkAvailable()) {
						service.startServerConnectionService();
					}
				}
			} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)
					|| action.equals(NetworkService.EVENT_NETWORK_SERVICE_ALARM)) {
				startService();
			}
		}
	}
	
	private void startService() {
		if (Session.getInstance().isServiceActive()) {
			if (appEx != null && service == null) {
				appEx.bindNetworkService();
			}
		}
	}
}
