package com.projectgoth.ui;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.YoutubeUri;
import com.projectgoth.common.migcommand.MigCommandsHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.util.AndroidLogger;


/**
 * @author Michael Joos
 *
 */
public class UrlHandler {
    
    private static final String  TAG = AndroidLogger.makeLogTag(UrlHandler.class);
    
    public static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" +    // switch on case insensitive matching
            "(" +       // begin group for schema
            "(?:http|https|file):\\/\\/" +
            "|(?:inline|data|about|javascript):" +
            ")" +
            "(.*)");
    
    public static final String MARKET_SEARCH = "market://search?q=pname:";
    
    
    public static void displayUrl(FragmentActivity activity, String url) {
        displayUrl(activity, url, null, 0);
    }

    public static void displayUrl(FragmentActivity activity, String url, String title, int titleIcon) {
        if (url.startsWith(Constants.LINK_MIG33)) {
            MigCommandsHandler.getInstance().handleCommandForUrl(url);
        } else if (!startActivityForUrl(activity, url)) {
            ActionHandler.getInstance().displayBrowser(activity, url, title, titleIcon);
        }
    }

    /**
     * Starts an external activity for the given URL.
     * @param url   The URL to process.
     * @return  true if an activity was started and false otherwise.
     */
    public static boolean startActivityForUrl(FragmentActivity activity, String url) {
        if (TextUtils.isEmpty(url)) {
            Logger.error.logWithTrace(TAG, UrlHandler.class, "URL is null");
            return false;
        }
        
        Intent intent;
        // perform generic parsing of the URI to turn it into an Intent.
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Logger.error.log(TAG, "Bad URI ", url, ": ", ex.getMessage());
            return false;
        }
        
        if (activity == null || activity.getPackageManager() == null) {
            return false;
        }

        // check whether the intent can be resolved. If not, we will see
        // whether we can download it from the Market.
        ResolveInfo resolveInfo = activity.getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) {
            String packagename = intent.getPackage();
            if (packagename != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_SEARCH + packagename));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                activity.startActivityForResult(intent, -1, null);
                return true;
            } else {
                return false;
            }
        }

        // sanitize the Intent, ensuring web pages can not bypass browser
        // security (only access to BROWSABLE activities).
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);
        
        // Make sure webkit can handle it internally before checking for
        // specialized handlers. If webkit can't handle it internally, we need to call
        // startActivityIfNeeded
        Matcher m = ACCEPTED_URI_SCHEMA.matcher(url);
        if (m.matches() && !isSpecializedHandlerAvailable(activity, intent)) {
            return false;
        }
        try {
            if (activity.startActivityIfNeeded(intent, -1)) {
                return true;
            }
        } catch (ActivityNotFoundException ex) {
            // ignore the error. If no application can handle the URL,
            // eg about:blank, assume the browser can handle it.
        }
        return false;
    }
    
    /**
     * Search for intent handlers that are specific to this URL. 
     * E.g: specialized apps like google maps or YouTube.
     */
    private static boolean isSpecializedHandlerAvailable(Activity activity, Intent intent) {
        final PackageManager pm = activity.getPackageManager();
        final List<ResolveInfo> handlers = pm.queryIntentActivities(intent,
                PackageManager.GET_RESOLVED_FILTER);
        if (handlers == null || handlers.size() == 0) {
            return false;
        }
        for (final ResolveInfo resolveInfo : handlers) {
            final IntentFilter filter = resolveInfo.filter;
            if (filter == null) {
                // No intent filter matches this intent?
                // Error on the side of staying in the browser, ignore
                continue;
            }
            if (filter.countDataAuthorities() == 0 && filter.countDataPaths() == 0) {
                // Generic handler, skip
                continue;
            }
            
            final ActivityInfo info = resolveInfo.activityInfo;
            if (info != null) {
                if (info.packageName.equalsIgnoreCase(activity.getPackageName())) {
                    return false;
                } else {
                    if (YoutubeUri.isYoutubeUrl(intent.getData().toString()) &&
                        info.packageName.toLowerCase().contains(".youtube")) {

                        intent.setClassName(info.packageName, info.name);
                    }
                }
            }
            
            return true;
        }
        return false;
    }

}
