/**
 * Copyright (c) 2013 Project Goth
 *
 * SystemController.java.java
 * Created Jun 10, 2013, 5:26:51 PM
 */

package com.projectgoth.controller;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.CaptchaListener;
import com.projectgoth.nemesis.listeners.GetSystemLanguageListener;
import com.projectgoth.nemesis.listeners.LoginCaptchaListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * @author cherryv
 * 
 */
public class SystemController {

    private Captcha                       mCaptcha;
    private LoginCaptchaListener          mCaptchaListener;

    private static final byte[]           sLock = new byte[0];
    
    private static final SystemController sInstance = new SystemController();
    
    private SystemController() {
    }

    public synchronized static SystemController getInstance() {
        return sInstance;
    }
    
    public void sendCaptchaResponse(String response) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null && response != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendCaptchaResponse(new CaptchaListener() {

                    @Override
                    public void onCaptchaResponse(Captcha captcha) {
                    }

                    @Override
                    public void onCaptchaOkResponse() {
                        // TODO: show a toast message?
                    }

                    @Override
                    public void onError(MigError error) {
                        // TODO: handle error
                    }
                }, response);
            }
        }
    }

    public Captcha getCaptcha() {
        synchronized (sLock) {
            return mCaptcha;
        }
    }

    public void setCaptcha(Captcha captcha) {
        synchronized (sLock) {
            if (this.mCaptcha != captcha) {
                this.mCaptcha = captcha;
                if (captcha != null) {
                    notifyCaptchaReceived(captcha);
                }
            }
        }

    }

    public void notifyCaptchaReceived (Captcha captcha) {
        mCaptchaListener.onCaptchaReceived(captcha);
    }

    public void setLoginCaptchaListener (LoginCaptchaListener listener) {
        mCaptchaListener = listener;
    }

    public void sendAction(String actionUrl, String contentType, String httpMethod) {
        AlertsDatastore.getInstance().requestSendAlertAction(actionUrl, contentType, httpMethod);
    }

    public void requestGetSystemLanguageList(GetSystemLanguageListener listener) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.getSystemLanguageList(listener);
            }
        }
    }

}

