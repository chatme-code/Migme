package com.projectgoth.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.PostsDatastore;

/**
 * Show splash UI and initial essential modules here to avoid white page
 * Created by freddie on 15/5/5.
 */
public class SplashActivity extends Activity {

    private Runnable mLazyInitiization = new Runnable() {
        @Override
        public void run() {
            lazyInitiize();
        }
    };
    private static final int SPLASH_DISPLAY_DELAY = 300; //300ms
    private static boolean sIsSplashDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler(Looper.getMainLooper());
        ImageView imageBackground = (ImageView) findViewById(R.id.splash_background);
        //let splash screen sustains a least 300ms, otherwise it may only "splash"
        handler.postDelayed(mLazyInitiization, SPLASH_DISPLAY_DELAY);
        imageBackground.setVisibility(View.GONE);
    }

    private void lazyInitiize() {
        Intent receivedIntent = getIntent();
        String action = receivedIntent.getAction();
        int flags = receivedIntent.getFlags();
        boolean isFromLauncher = receivedIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null
                && action.equals(Intent.ACTION_MAIN);
        boolean isLaunchFromHistory = (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        Intent launchMainDrawer;
        if (isFromLauncher || isLaunchFromHistory) {
            launchMainDrawer = new Intent(this, MainDrawerLayoutActivity.class);
        } else {
            launchMainDrawer = new Intent(receivedIntent);
            launchMainDrawer.setComponent(new ComponentName(this, MainDrawerLayoutActivity.class));
        }

        ApplicationEx app = ApplicationEx.getInstance();
        if (!app.isIsInitilized()) {
            app.initialize();
        }

        app.getNotificationHandler();
        AlertsDatastore.getInstance();
        PostsDatastore.getInstance();
        ChatDatastore.getInstance();
        startActivity(launchMainDrawer);
        sIsSplashDisplayed = true;
        finish();
    }

    public static void resetSplashDisplay() {
        sIsSplashDisplayed = false;
    }

    public static boolean isSplashDisplayed() {
        return sIsSplashDisplayed;
    }
}
