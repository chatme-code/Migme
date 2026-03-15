package com.projectgoth.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.LoginActivity;

/**
 * Created by danielchen on 15/4/22.
 */
public class ProgressDialogController {

    private static ProgressDialogController sInstance;
    private        ProgressDialog           mProgressDialog;
    private        boolean                  mInUsed             = false;
    private final  String                   LOG_TAG             = ProgressDialogController.class.getSimpleName();
    public enum ProgressType {Loading, Signingin, Signingout, Search, Connecting, Hangon, Creating};

    public static synchronized ProgressDialogController getInstance() {
        if (sInstance == null) {
            sInstance = new ProgressDialogController();
        }
        return sInstance;
    };

    /**
     * Show android default progress dialog
     *
     * @param context       activity to handle ui
     * @param progressType  mapping with i18n string to show
     */
    public void showProgressDialog(Context context, ProgressType progressType) {
        String message = null;
        if(progressType == ProgressType.Loading) {
            //hardcode for excluding perloaded webview of LoginActivity
            if (context instanceof LoginActivity) {
                return;
            }
            message = I18n.tr("Loading");
        } else if (progressType == ProgressType.Signingin) {
            message = I18n.tr("Signing in");
        } else if (progressType == ProgressType.Signingout) {
            message = I18n.tr("Signing out");
        } else if (progressType == ProgressType.Search) {
            message = I18n.tr("Search");
        } else if (progressType == ProgressType.Connecting) {
            message = I18n.tr("Connecting");
        } else if (progressType == ProgressType.Hangon) {
            message = I18n.tr("Hang on");
        } else if (progressType == ProgressType.Creating) {
            message = I18n.tr("Creating");
        }

        if (!mInUsed) {
            Logger.debug.log(LOG_TAG, "showLoadingProgressDialog -> ", context);
            mInUsed = true;
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    /**
     * Hide progress dialog in used
     */
    public void hideProgressDialog() {
        if (mInUsed && mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            mInUsed = false;
        }
    }
}
