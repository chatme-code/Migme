/**
 * Copyright (c) 2013 Project Goth
 *
 * FragmentActivityEx.java.java
 * Created May 30, 2013, 1:03:32 AM
 */

package com.projectgoth.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.FragmentEventListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cherryv
 * 
 */
public abstract class BaseFragmentActivity extends ActionBarActivity implements FragmentEventListener {

    public final static Object                      currentActivityLock = new Object();
    protected Map<String, WeakReference<Fragment>>  mAttachedFragments;
    private ProgressDialog                          mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fix for DROID-2829: The super.onCreate() method effectively starts
        // the fragments lifecycle (if there were any attached to it before it
        // was destroyed). So it's possible that the fragment callback methods
        // like onAttach() will be called before this onCreate() method is
        // finished. This can cause possible NPE as we nullify the
        // mAttachedFragments when the activity is destroyed. Initializing it
        // before the call to super.onCreate() ensures that it is not null
        // before it is used.
        // Reference:
        // http://stackoverflow.com/questions/14595946/activity-and-fragment-lifecycles-and-orientation-changes
        mAttachedFragments = new HashMap<String, WeakReference<Fragment>>();

        super.onCreate(savedInstanceState);
        ApplicationEx.initializeAppProperties(this);
        ApplicationEx applicationEx = ApplicationEx.getInstance();
        if (!applicationEx.isIsInitilized()) {
            launchSplashActivity();
            finish();
        }
    }

    protected void launchSplashActivity() {
        Intent receivedIntent = getIntent();
        Intent splashActivity = new Intent(receivedIntent);
        splashActivity.setComponent(new ComponentName(this, SplashActivity.class));
        startActivity(splashActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationEx.activityResumed();
        // TODO: Determine later on if we need to move this onCreate() method
        // instead
        // Use case: if activity is launched from outside the application
        synchronized (currentActivityLock) {
            ApplicationEx.getInstance().setCurrentActivity(this);
        }

        registerReceivers();

        ApplicationEx.getInstance().getDeviceHandler().stopActivityTransitionTimer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationEx.activityPaused();
        unregisterReceivers();

        ApplicationEx.getInstance().getDeviceHandler().startActivityTransitionTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAttachedFragments = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ApplicationEx.initializeAppProperties(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass(), fragment.getTag());
        super.onAttachFragment(fragment);

        if (fragment instanceof BaseFragment) {
            synchronized (mAttachedFragments) {
                String fragmentTag = fragment.getTag();
                mAttachedFragments.put(fragmentTag, new WeakReference<Fragment>(fragment));
            }
        }
    }

    @Override
    public void onDetachFragment(String fragmentTag) {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass(), fragmentTag);
        synchronized (mAttachedFragments) {
            mAttachedFragments.remove(fragmentTag);
        }
    }

    protected boolean isFragmentAttached(String fragmentTag) {
        synchronized (mAttachedFragments) {
            return mAttachedFragments.containsKey(fragmentTag);
        }
    }

    /**
     * Automatically called when the activity is created (
     * {@link #onCreate(Bundle)}) Implement to register broadcast events that
     * will be handled by this Activity
     */
    protected abstract void registerReceivers();

    /**
     * Automatically called when the activity is destroyed ({@link #onDestroy()}
     * ) Implement to remove the broadcast receiver of this Activity
     */
    protected abstract void unregisterReceivers();

    /**
     * Utility method to allow fragments to register for broadcast events
     * through this activity
     * 
     * @param receiver
     * @param event
     */
    public void registerFragmentReceiver(BroadcastReceiver receiver, IntentFilter event) {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, event);
    }

    /**
     * Utility method to allow fragments to remove its broadcast receiver from
     * this activity
     * 
     * @param receiver
     */
    public void unregisterFragmentReceiver(BroadcastReceiver receiver) {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Logger.error.log(getClass(), e);
        }
    }
    
    public void showProgressDialog(String title, String message, boolean cancelable) {
        dismissProgressDialog();

        mProgressDialog = ProgressDialog.show(this, title, message, true, cancelable);
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            try {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } catch (Exception e) {
                Logger.error.log(getClass(), e);
            }
            mProgressDialog = null;
        }
    }

    protected void showTransition(final int entryAnim, final int exitAnim) {
        overridePendingTransition(entryAnim, exitAnim);
    }
    
}
