package com.projectgoth.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetLocationCountryListener;

/**
 * Created by justinhsu on 4/22/15.
 */
public class NetworkUtils {
    public static String getLocalIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static String getCountryCodeBySim(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    public static String getCountryCodeByNetwork(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso();
    }

    public static String getCountryCode(Context context) {
        String code = "";
        /*
         * the rule of get country code
         * 1. get country code from sim
         * 2. get country code from IP
         * 3. get country code from locale
         */
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (!tm.getSimCountryIso().isEmpty()) {
            code = tm.getSimCountryIso();
        } else if (!tm.getNetworkCountryIso().isEmpty()) {
            code = tm.getNetworkCountryIso();
        } else if (!TextUtils.isEmpty(ApplicationEx.sCountryCode)) {
            code = ApplicationEx.sCountryCode;
        } else {
            code = context.getResources().getConfiguration().locale.getCountry();
        }
        return code;
    }


    public static void getCountryCodeByIp(GetLocationCountryListener listener) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            ApplicationEx.getInstance().getRequestManager().getCountryCodeByIP(listener);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
