/**
 * Copyright (c) 2013 Project Goth
 *
 * MainDrawerLayoutActivity.java
 * Created May 30, 2013, 12:20:41 AM
 */

package com.projectgoth.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.projectgoth.BuildConfig;
import com.deezer.sdk.player.event.PlayerState;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Version;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.notification.system.ShortcutBadgeException;
import com.projectgoth.notification.system.ShortcutBadger;
import com.projectgoth.ui.fragment.NavigationDrawerFragment;
import com.projectgoth.ui.listener.DispatchTouchEventBroadcaster;
import com.projectgoth.ui.listener.DispathTouchListener;
import com.projectgoth.util.AndroidLogger;

/**
 * @author cherryv
 * 
 */
public class MainDrawerLayoutActivity extends BaseCustomFragmentActivity implements DispatchTouchEventBroadcaster {

    protected List<DispathTouchListener> dispatchTouchListeners = new ArrayList<DispathTouchListener>();

    private boolean                      isIntentActionHandled  = false;
    private final static String          ACTION_HANDLED         = "ACTION_HANDLED";
    private Handler                      mHandler;
    private Runnable                     mRunnable;
    private final int                    mDelaySyncMS           = 10000;
    private final static String          mMigmeScheme           = "migme";
    private final static String          mMigmeVerifyHost       = "verify";
    private boolean                      mIsBackPressed         = false;

    private static final String          LOG_TAG                = AndroidLogger.makeLogTag(MainDrawerLayoutActivity.class);
    private boolean                      mIsCreated             = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Logger.debug.log(LOG_TAG, "onCreate:" + this);
        final Intent intent = getIntent();
        final String intentAction = intent.getAction();
        // Prevent launching multiple instance of the app when clicking from
        // Home
        boolean isFromLauncher = intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null
                && intentAction.equals(Intent.ACTION_MAIN);

        if (!isTaskRoot() && isFromLauncher) {
            finish();
            return;
        }

        ApplicationEx applicationEx = ApplicationEx.getInstance();
        if (!SplashActivity.isSplashDisplayed()) {
            launchSplashActivity();
            finish();
            return;
        }

        boolean isLoggedin = Session.getInstance().isLoggedIn();

        // Display login screen if it's not logged in yet
        // for non-login users to display this page.
        if (!isLoggedin && !ApplicationEx.getInstance().getPreviewStatus()) {
            setVisible(false);
            if (SystemDatastore.getInstance().isFirstTimeLog(GAEvent.Signin_Land.toString())) {
                GAEvent.Signin_Land.send(Version.getVasTrackingId());
            }
            FragmentHandler.getInstance().showLoginActivity(this, getRegisterToken());
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        moveDrawerToTop();

        FragmentHandler.getInstance().initMainDrawerLayoutFragments(getSupportFragmentManager());

        // Start the thread to handle system notifications
        ApplicationEx.getInstance().getNotificationHandler();

        // start to delay sync addressbook from content provider and upload to
        // server
        mHandler = new Handler();
        mRunnable = new Runnable() {

            @Override
            public void run() {
                ActionHandler.getInstance().beginRetrieveAddressBookContacts();
            }
        };
        mHandler.postDelayed(mRunnable, mDelaySyncMS);

        //get deezer music info
        if (BuildConfig.FLAVOR.equals("full")) {
            MusicDatastore.getInstance().getDeezerInfo();
        }

        //remove deezer notification panel if exist with no instance (crash or remove from recent app).
        PlayerState playState = DeezerPlayerManager.getInstance().getPlayerState();
        if (playState == PlayerState.STARTED) {
            NativeNotificationManager.getInstance().clearNotify(NativeNotificationManager.PLAYER_NOTIFICATION_ID);
        }

        //remove the badge number.
        try {
            ShortcutBadger.setBadge(ApplicationEx.getInstance(), 0);
            NativeNotificationManager.getInstance().setBadge(0);
        } catch (ShortcutBadgeException e) {
            e.printStackTrace();
        }

        mIsCreated = true;
    }

    private boolean isUrlScheme(Intent intent) {
        return intent != null && intent.getScheme() != null && intent.getScheme().equals(mMigmeScheme);
    }

    private boolean isTokenExist(Uri uri) {
        if (uri != null) {
            String host = uri.getHost();
            if (!TextUtils.isEmpty(host) && host.equals(mMigmeVerifyHost) && !uri.getPath().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getRegisterToken() {
        String registrationToken = null;
        Intent intent = getIntent();
        if (isUrlScheme(intent)) {
            Uri intentData = intent.getData();
            if (isTokenExist(intentData)) {
                registrationToken = intentData.getPath().substring(1);
            }
        }
        return registrationToken;
    }

    private void moveDrawerToTop() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.decor, null);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(
                R.id.navigation_drawer);
        mNavigationDrawerFragment.setup(drawer);

        // HACK: "steal" the first child of decor view
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        drawer.addView(child, 0);
        drawer.findViewById(R.id.navigation_drawer).setPadding(0, getStatusBarHeight(), 0, 0);

        // Make the drawer replace the first child
        decor.addView(drawer);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        Logger.debug.log(LOG_TAG, "onPostCreate:" + this + " isIntentActionHandled:" + isIntentActionHandled);

        // for this case :
        // activity killed by system->Intent saved -> activity recreated -> no need to start action again.
        if (!isIntentActionHandled) {
            // If application was opened from notification manager, we need to
            // handle it properly.
            processIntentAction(getIntent());
            isIntentActionHandled = true;
        }

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.debug.log(LOG_TAG, "onRestoreInstanceState:" + this);
        super.onRestoreInstanceState(savedInstanceState);
        isIntentActionHandled = savedInstanceState.getBoolean(ACTION_HANDLED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.debug.log(LOG_TAG, "onSaveInstanceState:" + this);
        super.onSaveInstanceState(outState);
        outState.putBoolean(ACTION_HANDLED, isIntentActionHandled);
    }


    @Override
    protected void onResume() {
        Logger.debug.log(LOG_TAG, "onResume:" + this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.debug.log(LOG_TAG, "onPause:" + this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Logger.debug.log(LOG_TAG, "onDestroy:" + this);
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = null;
        mHandler = null;

        //dismiss deezer notification panel if user remove instance from recent app
        if (!mIsBackPressed) {
            NativeNotificationManager.getInstance().clearNotify(NativeNotificationManager.PLAYER_NOTIFICATION_ID);
        }

        //reset splash screen when MainDraweLayoutActivity destroyed. For low-end device, it would have better user experience for launch time.
        if (mIsCreated) {
            SplashActivity.resetSplashDisplay();
        }

        super.onDestroy();
    }

    public void toggleDrawer() {
        mNavigationDrawerFragment.toggleDrawer();
    }

    @Override
    public void addDispatchTouchListener(DispathTouchListener listener) {
        dispatchTouchListeners.add(listener);
    }

    @Override
    public void removeDispatchTouchListener(DispathTouchListener listener) {
        dispatchTouchListeners.remove(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean dispatched = false;
        for (DispathTouchListener listener : dispatchTouchListeners) {
            dispatched |= listener.onDispatchTouchEvent(ev);
        }
        return !dispatched && super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        mIsBackPressed = true;
        super.onBackPressed();
    }

}
