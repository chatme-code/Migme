package com.projectgoth.controller;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.i18n.I18n;
import com.projectgoth.service.NetworkService;

/**
 * Created by houdangui on 6/4/15.
 */
public class StatusBarController {

    private Activity activity;

    private static final int RESET_STATUS_DELAY = 3 * 1000;

    private Handler mResetStatusHandler = new Handler();
    private Runnable mResetStatusTask = new Runnable() {
        @Override
        public void run() {
            // Dismiss the status bar when time has arrived
            statusBarState = StatusBarState.CONNECTION_AVAILABLE;
            updateStatusBar();
        }
    };


    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    enum StatusBarState {
        CONNECTION_AVAILABLE,
        NO_CONNECTION,
        CONNECTION_BACK,
        SLOW_CONNECTION
    }

    private StatusBarState statusBarState = StatusBarState.CONNECTION_AVAILABLE;

    private final static StatusBarController INSTANCE = new StatusBarController();

    /**
     * Constructor
     */
    private StatusBarController() {
    }

    /**
     * A single point of entry for this controller.
     * @return An instance of the controller.
     */
    public static synchronized StatusBarController getInstance() {
        return INSTANCE;
    }


    public void updateOnConnectionStatusChange() {
        NetworkService networkService = ApplicationEx.getInstance().getNetworkService();
        if (networkService != null) {

            if(networkService.isNetworkAvailable()) {
                if (statusBarState == StatusBarState.NO_CONNECTION) {
                    updateConnectionStatus(StatusBarState.CONNECTION_BACK);
                } else {
                    updateConnectionStatus(StatusBarState.CONNECTION_AVAILABLE);
                }
            } else {
                updateConnectionStatus(StatusBarState.NO_CONNECTION);
            }
        }
    }

    public void updateSlowConnectionStatus() {
        updateConnectionStatus(StatusBarState.SLOW_CONNECTION);
    }


    public void updateConnectionStatus(StatusBarState stateToUpdate) {

        //update the state when necessary
        switch (stateToUpdate) {

            case NO_CONNECTION:
                cancelPreviousScheduledTask();
                statusBarState = stateToUpdate;
                break;
            case CONNECTION_AVAILABLE:
                if (statusBarState == StatusBarState.NO_CONNECTION) {
                    statusBarState = stateToUpdate;
                }
                break;
            case CONNECTION_BACK:
                switch (statusBarState) {
                    case CONNECTION_AVAILABLE:
                        break;
                    case CONNECTION_BACK:
                        break;
                    case NO_CONNECTION:
                        statusBarState = stateToUpdate;
                        scheduleResetStatus();
                        break;
                    case SLOW_CONNECTION:
                        break;
                }
                break;
            case SLOW_CONNECTION:
                switch (statusBarState) {
                    case CONNECTION_AVAILABLE:
                        statusBarState = stateToUpdate;
                        scheduleResetStatus();
                        break;
                    case CONNECTION_BACK:
                        statusBarState = stateToUpdate;
                        scheduleResetStatus();
                        break;
                    case NO_CONNECTION:
                        break;
                    case SLOW_CONNECTION:
                        break;
                }
                break;
        }

        updateStatusBar();

    }

    public void updateStatusBar() {
        if (activity == null) {
            return;
        }
        switch (statusBarState) {
            case CONNECTION_AVAILABLE:
                dismissConnectionStatusBar();
                break;
            case CONNECTION_BACK:
                showConnectionBack();
                break;
            case NO_CONNECTION:
                showNoConnectionStatus();
                break;
            case SLOW_CONNECTION:
                showSlowConnection();
                break;
        }
    }

    private void showNoConnectionStatus() {
        TextView textView = (TextView) activity.findViewById(R.id.connection_status);
        if (textView == null)
            return;
        textView.setVisibility(View.VISIBLE);
        textView.setText(I18n.tr("Oops, no internet connection."));
        textView.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
        textView.setBackgroundColor(ApplicationEx.getColor(R.color.no_connection));
    }

    public void showNoConnectionStatus(Fragment fragment) {
        if (fragment != null) {
            TextView textView = (TextView) fragment.getView().findViewById(R.id.connection_status);
            if (textView != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(I18n.tr("Oops, no internet connection."));
                textView.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
                textView.setBackgroundColor(ApplicationEx.getColor(R.color.no_connection));
            }
        }
    }

    private void dismissConnectionStatusBar() {
        TextView textView = (TextView) activity.findViewById(R.id.connection_status);
        if (textView == null)
            return;
        textView.setVisibility(View.GONE);
    }

    public void dismissConnectionStatusBar(Fragment fragment) {
        if (fragment != null) {
            TextView textView = (TextView) fragment.getView().findViewById(R.id.connection_status);
            if (textView != null) {
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void showConnectionBack() {
        TextView textView = (TextView) activity.findViewById(R.id.connection_status);
        if (textView == null)
            return;
        textView.setVisibility(View.VISIBLE);
        textView.setText(I18n.tr("Woohoo, we are connected!"));
        textView.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
        textView.setBackgroundColor(ApplicationEx.getColor(R.color.connection_back));

    }

    private void showSlowConnection() {
        TextView textView = (TextView) activity.findViewById(R.id.connection_status);
        if (textView == null)
            return;
        textView.setVisibility(View.VISIBLE);
        textView.setText(I18n.tr("Slow connection."));
        textView.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
        textView.setBackgroundColor(ApplicationEx.getColor(R.color.slow_connection));
    }

    private void scheduleResetStatus() {
        cancelPreviousScheduledTask();
        if (mResetStatusHandler != null && mResetStatusTask != null) {
            mResetStatusHandler.postDelayed(mResetStatusTask, RESET_STATUS_DELAY);
        }
    }

    private void cancelPreviousScheduledTask() {
        if (mResetStatusHandler != null && mResetStatusTask != null) {
            mResetStatusHandler.removeCallbacks(mResetStatusTask);
        }
    }
}
