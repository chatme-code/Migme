/**
 * Copyright (c) 2013 Project Goth
 *
 * LoginActivity.java.java
 * Created May 30, 2013, 12:24:32 AM
 */

package com.projectgoth.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mig33.diggle.events.Events;
import com.projectgoth.CaptchaNotFoundException;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Version;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.FacebookLoginController;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.listeners.CheckEmailValidationListener;
import com.projectgoth.nemesis.listeners.CleanAllFieldsListener;
import com.projectgoth.nemesis.listeners.CreateNewUserListener;
import com.projectgoth.nemesis.listeners.GetLocationCountryListener;
import com.projectgoth.nemesis.listeners.GetSuggestUsersListener;
import com.projectgoth.nemesis.listeners.ResendEmailListener;
import com.projectgoth.nemesis.listeners.VerifyRegisterTokenListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigRequest;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.BaseSignupFragment;
import com.projectgoth.ui.listener.KeyboardListener;
import com.projectgoth.ui.listener.OnLoadWebListener;
import com.projectgoth.ui.listener.SetSuccessEmailListener;
import com.projectgoth.ui.listener.SetBannerListener;
import com.projectgoth.ui.listener.RegenerateCaptchaListener;
import com.projectgoth.ui.listener.SetShowLoadingListener;
import com.projectgoth.ui.widget.RelativeLayoutEx;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.CrashlyticsLog;
import com.projectgoth.util.AndroidUtils;
import com.projectgoth.util.FiksuInterface;
import com.projectgoth.util.FiksuInterface.RecurringEvent;
import com.projectgoth.util.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cherryv
 */
public class LoginActivity extends BaseFragmentActivity implements OnClickListener, KeyboardListener {

    private final static String                                         MIG_TERMS_URL                               = "https://migxchat.net/terms";
    private final static String                                         MIG_PRIVACY_URL                             = "https://migxchat.net/privacy";
    private static final String                                         LOG_TAG                                     = AndroidLogger.makeLogTag(LoginActivity.class);
    private FacebookLoginController                                     mFacebookLogin;
    private Map<PreloadedFragmentKey, BaseFragment>                     mPreloadedFragments;
    private Bundle                                                      mSignupBundle;
    private PreloadedFragmentKey                                        mCurrentKey;
    private CreateNewUserListener                                       mCreateNewUserListener;
    private ResendEmailListener                                         mResendEmailListener;
    private VerifyRegisterTokenListener                                 mVerifyRegisterTokenListener;
    private String                                                      mCurrentCaptchaSession;
    private boolean                                                     mIsPopbakckForInput                         = false;
    private boolean                                                     mDisableBackPress                           = false;
    private HashMap<String, CleanAllFieldsListener>                     mSetCleanAllFieldsListeners                 = new HashMap<String, CleanAllFieldsListener>();
    private ArrayList<OnLoadWebListener>                                mOnLoadWebListeners                         = new ArrayList<OnLoadWebListener>();

    private HashMap<String, SetBannerListener>                          mSetBannerListeners                         = new HashMap<String, SetBannerListener>();
    private HashMap<String, RegenerateCaptchaListener>                  mSetRegenCaptchaListeners                   = new HashMap<String, RegenerateCaptchaListener>();
    private HashMap<String, SetShowLoadingListener>                     mSetLoadingListeners                        = new HashMap<String, SetShowLoadingListener>();
    private HashMap<String, SetSuccessEmailListener>                    mSetEmailListeners                          = new HashMap<String, SetSuccessEmailListener>();
    private ArrayList<PreloadedFragmentKey>                             mAllVerifyFragments                         = new ArrayList<PreloadedFragmentKey>();
    private boolean                                                     mIsGetCountryCodeReturn = false;

    private String                                                      mSignupTypeKey                              = "SignType";
    private String                                                      mSignupSharePreferencesKey                  = "SignupCondition";
    private String                                                      mSignupSharePreferencesBlockConditionKey    = "SignupBlockCondition";
    private final static String                                         mTempDuplicateKey                           = "Duplicate";
    private final static String                                         SIGNUP_REGISTRACTION_TYPE_NORMAL_KEY        = "email1";
    private final static String                                         SIGNUP_REGISTRACTION_TYPE_FACEBOOK_KEY      = "facebook";
    private final static String                                         REGISTRATION_KEY                            = "RegisterToken";
    private boolean                                                     mIsSecondAttapmtToVerifyToken               = false;
    private int                                                         mInvalidInputAttemptTimeToCallGA            = 3;
    private int                                                         mIncorrectCaptchaAttemptCount               = 0;
    private int                                                         mIncorrectUsernameAttemptCount              = 0;
    private int                                                         mIncorrectPasswordAttemptCount              = 0;
    private long                                                        mResponseStartTime                          = 0;
    private GetLocationCountryListener                                  mGetLocationCountryListener;
    public enum PreloadedFragmentKey {
        LOGIN, USERNAME, EMAIL, PASSWORD, CAPTCHA, SUCCESS, VERIFTYING_TOKEN,  TOKEN_USED, TOKEN_SUCCESS, TOKEN_EXPIRED, TOKEN_TIMEOUT, FORGOT_PASSWORD, DISCOVER, FACEBOOK_SUCCESS, FACEBOOK_FAIL
    }

    public enum SignupType {
        NORMAL, FACEBOOK
    }

    public enum DialogType {
        BACKPRESSED, ALERT
    }

    private enum RegistrationErrorType {
        CAPTCHA_INCORRECT(2001), CAPTCHA_NOT_FOUND(2002), CAPTCHA_NOT_PROVIDED(2003), INVALID_USERNAME(2004), INVALID_EMAIL(2005),
        INVALID_PASSWORD(2006), RATE_LIMIT_EXCEEDED(2007), USERNAME_USED(2008),
        EMAIL_USED(2009), INVALID_FACEBOOK_TOKEN(2010), FACEBOOK_TOKEN_VALIDATIOIN_FAIL(2011), ALL_OTHER_UNKNOWN(2099), NONE(-1);

        private int index;

        RegistrationErrorType(int index) {
            this.index = index;
        }

        private int getIndex() {
            return index;
        }

        private static RegistrationErrorType getTypeFromLong(long input) {
            for (RegistrationErrorType type : RegistrationErrorType.values()) {
                if (type.getIndex() == (int) input) {
                    return type;
                }
            }
            return NONE;
        }
    }

    public interface OnFragmentShowListener {
        void show();
    }

    private HashMap<PreloadedFragmentKey, OnFragmentShowListener> mOnFragmentShowListenerMap = new HashMap<PreloadedFragmentKey, OnFragmentShowListener>();
    public void addOnFragmentShowListener(PreloadedFragmentKey key, OnFragmentShowListener listener){
       mOnFragmentShowListenerMap.put(key, listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        RelativeLayoutEx mActivityView = (RelativeLayoutEx) findViewById(R.id.content_frame);
        mActivityView.setKeyboardListener(this);

        // Note : Important to always display login form when keyboard is shown.
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Initialize the fragments that need to be preloaded.
        if (mPreloadedFragments == null) {

            mPreloadedFragments = new HashMap<PreloadedFragmentKey, BaseFragment>();

            final ConnectionDetail detail = Config.getInstance().getConnectionDetail();
            mPreloadedFragments.put(PreloadedFragmentKey.DISCOVER, FragmentHandler.getInstance().getBrowserFragment(detail.getDiscoverServer(), true));
            mPreloadedFragments.put(PreloadedFragmentKey.FORGOT_PASSWORD, FragmentHandler.getInstance().getBrowserFragment(WebURL.URL_FORGOT_PASSWORD));
            mPreloadedFragments.put(PreloadedFragmentKey.LOGIN, FragmentHandler.getInstance().getLoginFragment());

            FragmentHandler fragmentHandler = FragmentHandler.getInstance();
            for (final PreloadedFragmentKey key : mPreloadedFragments.keySet()) {
                fragmentHandler.addFragment(this, mPreloadedFragments.get(key));
            }
        }
        mGetLocationCountryListener = new GetLocationCountryListener() {
            @Override
            public void onError(MigError.Type errType, long errno, String errMsg) {
                mIsGetCountryCodeReturn = true;
                String errorMsg = "";
                if (!TextUtils.isEmpty(errMsg)) {
                    errorMsg = errMsg;
                }
                Logger.info.log(LOG_TAG, "Can't get country code by IP with error : " + errorMsg);
            }

            @Override
            public void onLocationCountryReceived(String countryCode) {
                mIsGetCountryCodeReturn = true;
                if (!TextUtils.isEmpty(countryCode)) {
                    ApplicationEx.sCountryCode = countryCode;
                }
            }
        };

        mCreateNewUserListener = new CreateNewUserListener() {
            @Override
            public void onUserCreated(final boolean result) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        long interval = calculateResponsePeriod();
                        if (interval > 0) {
                            GAEvent.Signup_Timing_CreateUser.sendTiming(Version.getVasTrackingId(), interval);
                        }
                        triggerSetLoadingListener(mCurrentKey);
                        if (result) {
                            if (getCurrentSignupType() == SignupType.FACEBOOK) {
                                GAEvent.Signup_Facebook_Success.send(Version.getVasTrackingId());
                                GAEvent.Signup_Facebook_PasswordSuccess.send(Version.getVasTrackingId());
                                GAEvent.Signup_Facebook_UsernameSuccess.send(Version.getVasTrackingId());
                            } else {
                                GAEvent.Signup_Create_User_Success.send(Version.getVasTrackingId());
                                GAEvent.SignUp_PasswordSuccess.send(Version.getVasTrackingId());
                                GAEvent.SignUp_CaptchaSuccess.send(Version.getVasTrackingId());
                            }
                            disablePopbackForNewInputStatus();
                            hideKeyboard();
                            setCleanFailAttemptCount();
                            if (SaveToSignupSharePreference()) {
                                if (getCurrentSignupType() == SignupType.FACEBOOK) {
                                    showPreloadedFragment(PreloadedFragmentKey.FACEBOOK_SUCCESS);
                                    setVerifyCoditionFromSharePreference(false);
                                } else {
                                    showPreloadedFragment(PreloadedFragmentKey.SUCCESS, false);
                                    setVerifyCoditionFromSharePreference(true);
                                    triggerSetEmailListener(PreloadedFragmentKey.SUCCESS);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(final MigError.Type errType, final long errno, final String errMsg) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        long interval = calculateResponsePeriod();
                        if (interval > 0) {
                            GAEvent.Signup_Timing_CreateUser.sendTiming(Version.getVasTrackingId(), interval);
                        }
                        triggerSetLoadingListener(mCurrentKey);
                        onCreateUserError(errType, errno, errMsg);
                    }
                });
            }
        };

        if (getVerifyConditionFromSharePreference()) {
            generateFragment(PreloadedFragmentKey.CAPTCHA);
            showPreloadedFragment(PreloadedFragmentKey.SUCCESS);
        } else {
            showPreloadedFragment(PreloadedFragmentKey.LOGIN);
        }
        getIntentData();
    }

    public void disablePopbackForNewInputStatus() {
        mDisableBackPress = false;
        mIsPopbakckForInput = false;
    }

    public void enablePopbackForNewInputStatus() {
        mDisableBackPress = true;
        mIsPopbakckForInput = true;
    }

    public void getIntentData() {
        Intent intent = getIntent();
        if (intent.hasExtra(FragmentHandler.REGISTER_TOKEN_KEY)) {
            GAEvent.Singup_EmailVerifyRedirection.send(Version.getVasTrackingId());
            hideAllVerifyFragments();
            showPreloadedFragment(PreloadedFragmentKey.VERIFTYING_TOKEN);
            validateRegisterToken();
        } else if (intent.hasExtra(FragmentHandler.LOGIN_FRAGMENT)) {
            int index = intent.getIntExtra(FragmentHandler.LOGIN_FRAGMENT, PreloadedFragmentKey.LOGIN.ordinal());
            if (index == PreloadedFragmentKey.USERNAME.ordinal()) {
                showPreloadedFragment(PreloadedFragmentKey.USERNAME);
            }
        }
    }

    public void generateAllVerifyFragmentsList() {
        if (mAllVerifyFragments.isEmpty()) {
            mAllVerifyFragments.add(PreloadedFragmentKey.TOKEN_EXPIRED);
            mAllVerifyFragments.add(PreloadedFragmentKey.TOKEN_SUCCESS);
            mAllVerifyFragments.add(PreloadedFragmentKey.TOKEN_TIMEOUT);
            mAllVerifyFragments.add(PreloadedFragmentKey.TOKEN_USED);
            mAllVerifyFragments.add(PreloadedFragmentKey.VERIFTYING_TOKEN);
            mAllVerifyFragments.add(PreloadedFragmentKey.SUCCESS);
        }
    }

    public void hideAllVerifyFragments() {
        generateAllVerifyFragmentsList();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (final PreloadedFragmentKey key : mAllVerifyFragments) {
            if (mPreloadedFragments.containsKey(key)) {
                transaction.hide(getPreloadedFragment(key));
            }
        }
        transaction.commit();
    }

    private void hideKeyboard() {
        InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void onCreateUserError(MigError.Type errType, long errno, String errMsg) {
        if (errType == MigError.Type.HTTP_REQUEST_TIMEOUT) {
            GAEvent.Signup_API_TimeOut.send(Version.getVasTrackingId());
            triggerSetLoadingListener(PreloadedFragmentKey.CAPTCHA);
            Toast.makeText(LoginActivity.this, I18n.tr("Connection timeout, please try again later!"), Toast.LENGTH_SHORT).show();
            return;
        } else if (errType == MigError.Type.HTTP_WORKER_ERROR) {
            Toast.makeText(LoginActivity.this, I18n.tr("Cannot connect to server."), Toast.LENGTH_SHORT).show();
            triggerSetLoadingListener(PreloadedFragmentKey.CAPTCHA);
            return;
        }
        RegistrationErrorType type = RegistrationErrorType.getTypeFromLong(errno);
        switch (type) {
            case CAPTCHA_INCORRECT:
                mIncorrectCaptchaAttemptCount++;
                if (mIncorrectCaptchaAttemptCount >= mInvalidInputAttemptTimeToCallGA) {
                    GAEvent.SignUp_CaptchaRepeatFailure.send(Version.getVasTrackingId());
                }
                GAEvent.SignUp_CaptchaFailure.send(Version.getVasTrackingId());
                triggerErrorSetBannerListener(PreloadedFragmentKey.CAPTCHA);
            case CAPTCHA_NOT_FOUND: {
                //FIXME: add the log for server team to check no captcha not found issue.
                String captchaSolution = mSignupBundle.getString(PreloadedFragmentKey.CAPTCHA.toString());
                CrashlyticsLog.log(new CaptchaNotFoundException("No captcha found"), "cannot find captcha, captcha session: " + mCurrentCaptchaSession +
                        ", captcha text: " + captchaSolution);
            }

            case CAPTCHA_NOT_PROVIDED:
                setSignupData(PreloadedFragmentKey.CAPTCHA, "");
                triggerSetLoadingListener(PreloadedFragmentKey.CAPTCHA);
                popbackForNewInput(PreloadedFragmentKey.CAPTCHA);
                triggerCleanSignupInputFieldListener(PreloadedFragmentKey.CAPTCHA);
                break;
            case INVALID_USERNAME:
                mIncorrectUsernameAttemptCount++;
                if (mIncorrectUsernameAttemptCount >= mInvalidInputAttemptTimeToCallGA) {
                    GAEvent.SignUp_UsernameRepeatFailure.send(Version.getVasTrackingId());
                }
                popbackForNewInput(PreloadedFragmentKey.USERNAME);
                triggerErrorSetBannerListener(PreloadedFragmentKey.USERNAME);
                triggerSetLoadingListener(PreloadedFragmentKey.USERNAME);
                GAEvent.Signup_ErrorHandle_UsernameErrorType.sendWithErrorCode(Version.getVasTrackingId(), (short) errno);
                break;
            case INVALID_EMAIL:
                popbackForNewInput(PreloadedFragmentKey.EMAIL);
                triggerErrorSetBannerListener(PreloadedFragmentKey.EMAIL);
                triggerSetLoadingListener(PreloadedFragmentKey.EMAIL);
                GAEvent.Signup_ErrorHandle_EmailErrorType.send(Version.getVasTrackingId());
                break;
            case INVALID_PASSWORD:
                mIncorrectPasswordAttemptCount++;
                if (mIncorrectPasswordAttemptCount >= mInvalidInputAttemptTimeToCallGA) {
                    GAEvent.SignUp_PasswordRepeatFailure.send(Version.getVasTrackingId());
                }
                if (getCurrentSignupType() == SignupType.FACEBOOK) {
                    GAEvent.Signup_Facebook_PasswordFailure.send(Version.getVasTrackingId());
                } else {
                    GAEvent.SignUp_PasswordFailure.send(Version.getVasTrackingId());
                }
                popbackForNewInput(PreloadedFragmentKey.PASSWORD);
                triggerSetLoadingListener(PreloadedFragmentKey.PASSWORD);
                triggerErrorSetBannerListener(PreloadedFragmentKey.PASSWORD);
                break;
            case RATE_LIMIT_EXCEEDED:
                resetSignupData();
                triggerSetLoadingListener(PreloadedFragmentKey.USERNAME);
                showPreloadedFragment(PreloadedFragmentKey.USERNAME);
                showDialog(DialogType.ALERT);
                break;
            case USERNAME_USED:
                triggerSetLoadingListener(PreloadedFragmentKey.USERNAME);
                popbackForNewInput(PreloadedFragmentKey.USERNAME);
                triggerErrorSetBannerListener(PreloadedFragmentKey.USERNAME);
                break;
            case EMAIL_USED:
                triggerSetLoadingListener(PreloadedFragmentKey.EMAIL);
                popbackForNewInput(PreloadedFragmentKey.EMAIL);
                triggerErrorSetBannerListener(PreloadedFragmentKey.EMAIL);
                break;
            case INVALID_FACEBOOK_TOKEN:
            case FACEBOOK_TOKEN_VALIDATIOIN_FAIL:
                triggerSetLoadingListener(mCurrentKey);
                showPreloadedFragment(PreloadedFragmentKey.FACEBOOK_FAIL);
                break;
            case ALL_OTHER_UNKNOWN:
                GAEvent.Signup_Undistinguishable_Error.send(Version.getVasTrackingId());
                Log.e(LOG_TAG, String.format("errorType: %s, errorNo: %s, errorMsg: %s", errType.toString(), String.valueOf(errno), errMsg));
                break;
            case NONE:
                break;
            default:
                triggerSetLoadingListener(PreloadedFragmentKey.CAPTCHA);
                GAEvent.Signup_Undistinguishable_Error.send(Version.getVasTrackingId());
                Toast.makeText(this, I18n.tr("Server error."), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, String.format("errorType: %s, errorNo: %s, errorMsg: %s", errType.toString(), String.valueOf(errno), errMsg));
                break;

        }
    }

    private void generateFragment(PreloadedFragmentKey key) {
        PreloadedFragmentKey newKey = null;
        switch (key) {
            case USERNAME:
                String facebookEmail = FacebookLoginController.getInstance().getFacebookEmail();
                if (getCurrentSignupType() != SignupType.FACEBOOK || (TextUtils.isEmpty(facebookEmail) && getCurrentSignupType() == SignupType.FACEBOOK)) {
                    if (!mPreloadedFragments.containsKey(PreloadedFragmentKey.EMAIL)) {
                        mPreloadedFragments.put(PreloadedFragmentKey.EMAIL, FragmentHandler.getInstance().getSignupEmailFragment());
                        newKey = PreloadedFragmentKey.EMAIL;
                    }
                } else if (getCurrentSignupType() == SignupType.FACEBOOK) {
                    generateFragment(PreloadedFragmentKey.EMAIL);
                }
                break;
            case EMAIL:
                if (!mPreloadedFragments.containsKey(PreloadedFragmentKey.PASSWORD)) {
                    mPreloadedFragments.put(PreloadedFragmentKey.PASSWORD, FragmentHandler.getInstance().getSignupPasswordFragment());
                    newKey = PreloadedFragmentKey.PASSWORD;
                }
                break;
            case PASSWORD:
                if (getCurrentSignupType() != SignupType.FACEBOOK) {
                    if (!mPreloadedFragments.containsKey(PreloadedFragmentKey.CAPTCHA)) {
                        mPreloadedFragments.put(PreloadedFragmentKey.CAPTCHA, FragmentHandler.getInstance().getSignupVerifyFragment());
                        newKey = PreloadedFragmentKey.CAPTCHA;
                    }
                } else {
                    preloadFragmentForFacebookSignup();
                }
                break;
            case CAPTCHA:
                if (!mPreloadedFragments.containsKey(PreloadedFragmentKey.SUCCESS)) {
                    mPreloadedFragments.put(PreloadedFragmentKey.SUCCESS, FragmentHandler.getInstance().getSignupEmailVerifyFragment());
                    newKey = PreloadedFragmentKey.SUCCESS;
                }
                break;
            case VERIFTYING_TOKEN:
                preloadFragmentForVerify();
                break;
            case LOGIN:
                setSignupType(SignupType.NORMAL);
            case TOKEN_SUCCESS:
            case TOKEN_USED:
            case TOKEN_EXPIRED:
            case TOKEN_TIMEOUT:
            case FACEBOOK_FAIL:
            case FACEBOOK_SUCCESS:
                if (!mPreloadedFragments.containsKey(PreloadedFragmentKey.USERNAME)) {
                    mPreloadedFragments.put(PreloadedFragmentKey.USERNAME, FragmentHandler.getInstance().getSignupUsernameFragment());
                    newKey = PreloadedFragmentKey.USERNAME;
                }
                break;
        }
        if (newKey != null) {
            FragmentHandler.getInstance().addFragment(this, mPreloadedFragments.get(newKey));

        }
    }

    public void preloadFragmentForFacebookSignup() {
        ArrayList<PreloadedFragmentKey> facebookSignupPreload = new ArrayList<PreloadedFragmentKey>();
        facebookSignupPreload.add(PreloadedFragmentKey.FACEBOOK_FAIL);
        facebookSignupPreload.add(PreloadedFragmentKey.FACEBOOK_SUCCESS);
        mPreloadedFragments.put(PreloadedFragmentKey.FACEBOOK_FAIL, FragmentHandler.getInstance().getSignupFacebookFailFragment());
        mPreloadedFragments.put(PreloadedFragmentKey.FACEBOOK_SUCCESS, FragmentHandler.getInstance().getSignupFacebookSuccessFragment());
        for (final PreloadedFragmentKey key : facebookSignupPreload) {
            FragmentHandler.getInstance().addFragment(this, mPreloadedFragments.get(key));
        }
    }

    public void preloadFragmentForVerify() {
        ArrayList<PreloadedFragmentKey> verifyPreload = new ArrayList<PreloadedFragmentKey>();
        verifyPreload.add(PreloadedFragmentKey.VERIFTYING_TOKEN);
        verifyPreload.add(PreloadedFragmentKey.TOKEN_EXPIRED);
        verifyPreload.add(PreloadedFragmentKey.TOKEN_SUCCESS);
        verifyPreload.add(PreloadedFragmentKey.TOKEN_USED);
        verifyPreload.add(PreloadedFragmentKey.TOKEN_TIMEOUT);
        mPreloadedFragments.put(PreloadedFragmentKey.VERIFTYING_TOKEN, FragmentHandler.getInstance().getSignupEmailVerifingFragment());
        mPreloadedFragments.put(PreloadedFragmentKey.TOKEN_EXPIRED, FragmentHandler.getInstance().getSignupEmailResultExpiredFragment());
        mPreloadedFragments.put(PreloadedFragmentKey.TOKEN_SUCCESS, FragmentHandler.getInstance().getSignupEmailResultSuccessFragment());
        mPreloadedFragments.put(PreloadedFragmentKey.TOKEN_USED, FragmentHandler.getInstance().getSignupEmailResultUsedFragment());
        mPreloadedFragments.put(PreloadedFragmentKey.TOKEN_TIMEOUT, FragmentHandler.getInstance().getSignupEmailResultTimeoutFragment());
        for (final PreloadedFragmentKey key : verifyPreload) {
            FragmentHandler.getInstance().addFragment(this, mPreloadedFragments.get(key));
        }
    }

    @Override
    public void registerReceivers() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.Facebook.INITIALIZED));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.Facebook.ERROR));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.Facebook.ACCOUNT_LINK_ERROR));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(AppEvents.Application.SHOW_LOGIN));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.ERROR));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.SUCCESS));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Events.Login.PROGRESS));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(AppEvents.NetworkService.STARTED));
        localBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(AppEvents.NetworkService.NETWORK_STATUS_CHANGED));
    }

    @Override
    public void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onShowFragment(Fragment fragment) {
        updateViewStatusAccordingToConnectionStatus();
    }

    @Override
    public void onHideFragment(Fragment fragment) {
        //If an user intervenes in login progress, stop the fusion service.
        NetworkService networkService = ApplicationEx.getInstance().getNetworkService();
        if (!networkService.isLoggedIn()) {
            networkService.stopServerConnectionService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_discover:
                triggerOnLoadWebListener();
                GAEvent.Signin_PeekInside.send(Version.getVasTrackingId());
                GAEvent.SignUp_PeekInside.send(Version.getVasTrackingId());
                FiksuInterface.sendEvent(RecurringEvent.PreLogin_PeekButton, this);

                ApplicationEx.getInstance().setPreviewStatus(true);
                ActionHandler.getInstance().showMainActivityAfterLogin(this, false);
                break;
            case R.id.btn_forgotpassword:
                GAEvent.Signin_ForgetPassword.send(Version.getVasTrackingId());
                showPreloadedFragment(PreloadedFragmentKey.FORGOT_PASSWORD);
                break;
            case R.id.login_btn_signup:
                if (SystemDatastore.getInstance().isFirstTimeLog(GAEvent.Signin_SwitchSignUp.toString())) {
                    GAEvent.Signin_SwitchSignUp.send(Version.getVasTrackingId());
                }
                FiksuInterface.sendEvent(RecurringEvent.RegistrationScreen, this);
                showPreloadedFragment(PreloadedFragmentKey.USERNAME);
                break;
            case R.id.btn_login:
                showPreloadedFragment(LoginActivity.PreloadedFragmentKey.LOGIN, true);
                break;
            case R.id.txt_terms_of_use_link:
                GAEvent.SignUp_Term.send(Version.getVasTrackingId());
                ActionHandler.getInstance().displayBrowser(this, MIG_TERMS_URL);
                break;
            case R.id.txt_privacy_policy_link:
                GAEvent.SignUp_Privacy.send(Version.getVasTrackingId());
                ActionHandler.getInstance().displayBrowser(this, MIG_PRIVACY_URL);
                break;
            case R.id.btn_fbconnect:
                if (AndroidUtils.isApkInstalled(this, FacebookLoginController.FACEBOOK_PACKAGE_NAME)) {
                    if (SystemDatastore.getInstance().isFirstTimeLog(GAEvent.Signin_Facebook.toString())) {
                        GAEvent.Signin_Facebook.send(Version.getVasTrackingId());
                        setGAMonitorStartTime();
                    }
                    initiateFBlogin();
                } else {
                    Toast.makeText(this, I18n.tr("please install facebook app first"), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!TextUtils.isEmpty(intent.getStringExtra(FragmentHandler.REGISTER_TOKEN_KEY))) {
            setIntent(intent);
            getIntentData();
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                ActionHandler.getInstance().displayConnectionSettings(this);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        boolean shouldClearTask = this.getIntent().getBooleanExtra(FragmentHandler.CLEAR_TASK_IN_GINGERBREAD, false);
        if (shouldClearTask) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            signupPopback(mCurrentKey);
        }
    }

    public void signupPopback(PreloadedFragmentKey currentKey) {
        if (mDisableBackPress) {
            showDialog(DialogType.BACKPRESSED);
            return;
        }
        triggerSetLoadingListener(currentKey);
        switch (currentKey) {
            case LOGIN:
                super.onBackPressed();
                break;
            case USERNAME:
                showPreloadedFragment(PreloadedFragmentKey.LOGIN, true);
                break;
            case EMAIL:
                showPreloadedFragment(PreloadedFragmentKey.USERNAME, true);
                break;
            case PASSWORD:
                String facebookEmail = FacebookLoginController.getInstance().getFacebookEmail();
                if (getCurrentSignupType() != SignupType.FACEBOOK || (getCurrentSignupType() == SignupType.FACEBOOK && facebookEmail.isEmpty())) {
                    showPreloadedFragment(PreloadedFragmentKey.EMAIL, true);
                } else if (getCurrentSignupType() == SignupType.FACEBOOK) {
                    showPreloadedFragment(PreloadedFragmentKey.USERNAME, true);
                }
                break;
            case CAPTCHA:
                showPreloadedFragment(PreloadedFragmentKey.PASSWORD, true);
                break;
            case SUCCESS:
                break;
            case DISCOVER:
                showPreloadedFragment(PreloadedFragmentKey.LOGIN, false);
                break;
            case FORGOT_PASSWORD:
                showPreloadedFragment(PreloadedFragmentKey.LOGIN, false);
                break;
            case VERIFTYING_TOKEN:
                showPreloadedFragment(PreloadedFragmentKey.LOGIN, false);
                break;
            case TOKEN_EXPIRED:
                break;
            case TOKEN_TIMEOUT:
                break;
            case TOKEN_SUCCESS:
                break;
            case TOKEN_USED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onSoftKeyboardShown() {
    }

    @Override
    public void onSoftKeyboardHidden() {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (com.facebook.Session.getActiveSession() != null && data != null) {
            com.facebook.Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Events.Login.Facebook.INITIALIZED)) {
                showProgressDialog(I18n.tr("Connecting to Facebook"), I18n.tr("Almost there, hang on."), true);
            } else if (action.equals(Events.Login.SUCCESS)) {
            } else if (action.equals(Events.Login.ERROR)) {
                Logger.error.log(LOG_TAG, action);
                dismissProgressDialog();
            } else if (action.equals(Events.Login.Facebook.ERROR)) {
                dismissProgressDialog();
                Toast.makeText(context, I18n.tr("Server error happened, please try again later!"), Toast.LENGTH_SHORT).show();
                Logger.error.log(LOG_TAG, action);
            } else if (action.equals(AppEvents.Application.SHOW_LOGIN)) {
            } else if (action.equals(Events.Login.Facebook.ACCOUNT_LINK_ERROR)) {
                triggerResetAllBannerListener();
                setSignupType(SignupType.FACEBOOK);
                GAEvent.SignUp_Facebook.send(Version.getVasTrackingId());
                showPreloadedFragment(PreloadedFragmentKey.USERNAME);
                dismissProgressDialog();
            } else if (action.equals(Events.Login.PROGRESS)) {
                // Handle login progress here
            } else if (action.equals(AppEvents.NetworkService.STARTED) ||
                    action.equals(AppEvents.NetworkService.NETWORK_STATUS_CHANGED)) {

                // Update connection status bar and buttons according to connection status
                updateViewStatusAccordingToConnectionStatus();

            }

            if (action.equals(Events.Login.ERROR)) {
                // Handle login specific error here
            }
        }
    };

    public void initiateFBlogin() {
        mFacebookLogin = FacebookLoginController.getInstance();
        mFacebookLogin.setControllerContext(this);
        mFacebookLogin.initializeFBSession(null);
        mFacebookLogin.startLogin();
    }

    private BaseFragment getPreloadedFragment(final PreloadedFragmentKey key) {
        if (key != null) {
            return mPreloadedFragments.get(key);
        } else {
            Logger.warning.logWithTrace(LOG_TAG, getClass(), "null key");
        }
        return null;
    }

    public void showPreloadedFragment(final PreloadedFragmentKey keyToShow) {
        showPreloadedFragment(keyToShow, false);
    }

    public void showPreloadedFragment(final PreloadedFragmentKey keyToShow, boolean isBack) {

        mCurrentKey = keyToShow;

        if (keyToShow != null) {
            try {
                if (!isBack) {
                    generateFragment(keyToShow);
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Set fragment transaction animation
                if ((!isBack || mCurrentKey == PreloadedFragmentKey.DISCOVER || mCurrentKey == PreloadedFragmentKey.FORGOT_PASSWORD)
                        && mCurrentKey != PreloadedFragmentKey.LOGIN && mCurrentKey != PreloadedFragmentKey.USERNAME) {
                    transaction.setCustomAnimations(R.anim.login_push_right_in, R.anim.login_push_left_out);
                } else if (mCurrentKey != PreloadedFragmentKey.USERNAME && mCurrentKey != PreloadedFragmentKey.LOGIN) {
                    transaction.setCustomAnimations(R.anim.login_push_left_in, R.anim.login_push_right_out);
                }

                // Show the keyToShow fragment, hide the others
                for (final PreloadedFragmentKey key : mPreloadedFragments.keySet()) {
                    final BaseFragment preloadedFragment = getPreloadedFragment(key);
                    if (preloadedFragment != null) {
                        if (key.equals(keyToShow)) {
                            transaction.show(preloadedFragment);
                        } else {
                            transaction.hide(preloadedFragment);
                        }
                    }
                }

                if (keyToShow == PreloadedFragmentKey.LOGIN) {
                    resetSignupData();
                    triggerResetAllBannerListener();
                }

                transaction.commit();

                //callback to fragment to handle ui behavior
                if (mOnFragmentShowListenerMap.containsKey(keyToShow)) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            mOnFragmentShowListenerMap.get(keyToShow).show();
                        }
                    });
                }
            } catch (IllegalStateException ex) {
                // Logging this error to crashlytics, so we get more information on this issue.
                Crashlytics.log(Log.ERROR, LOG_TAG,
                        "Preloaded fragment to be shown: " + ((keyToShow == null) ? "null" : keyToShow.toString()) +
                                "Preloaded fragment cache count: " + ((mPreloadedFragments == null) ? "null" : mPreloadedFragments.size()) +
                                ex.getMessage());
            }
        } else {
            Logger.warning.logWithTrace(LOG_TAG, getClass(), "null key");
        }
    }

    private void updateViewStatusAccordingToConnectionStatus() {

        // Update connection status bar and buttons according to connection status
        NetworkService networkService = ApplicationEx.getInstance().getNetworkService();
        if (networkService != null && mAttachedFragments != null) {
            for (Map.Entry<String, WeakReference<Fragment>> entry: mAttachedFragments.entrySet()) {
                Fragment fragment = entry.getValue().get();
                if (fragment != null && fragment instanceof BaseFragment) {
                    if (networkService.isNetworkAvailable()) {
                        ((BaseFragment)fragment).enableNoConnectionDisableButton();
                        ((BaseFragment)fragment).dismissConnectionStatusBar();
                    } else {
                        ((BaseFragment)fragment).disableNoConnectionDisableButton();
                        ((BaseFragment)fragment).showUpConnectionStatusBar();
                    }
                }
            }
        }
    }

    public void resetSignupData() {
        mSignupBundle = new Bundle();
        triggerCleanAllSignupInputFieldsListener();
    }

    public void setSignupData(PreloadedFragmentKey key, String input) {
        if (mSignupBundle == null) {
            mSignupBundle = new Bundle();
        }
        if (input != null) {
            mSignupBundle.putString(key.toString(), input);
        }
    }

    public Bundle getSignupBundle() {
        return mSignupBundle;
    }

    public String getSignupData(PreloadedFragmentKey key) {
        String value = "";
        if (mSignupBundle != null) {
            value = mSignupBundle.getString(key.toString());
        }
        return value;
    }

    public SignupType getCurrentSignupType() {
        SignupType returnType = SignupType.NORMAL;
        if (mSignupBundle != null) {
            int type = mSignupBundle.getInt(mSignupTypeKey);
            if (type == SignupType.NORMAL.ordinal()) {
                returnType = SignupType.NORMAL;
            } else if (type == SignupType.FACEBOOK.ordinal()) {
                returnType = SignupType.FACEBOOK;
            }
        }
        return returnType;
    }

    public void setSignupType(SignupType type) {
        if (mSignupBundle == null) {
            mSignupBundle = new Bundle();
        }
        mSignupBundle.putInt(mSignupTypeKey, type.ordinal());
        String facebookEmail = FacebookLoginController.getInstance().getFacebookEmail();
        if (getCurrentSignupType() == SignupType.FACEBOOK && !TextUtils.isEmpty(facebookEmail)) {
            setSignupData(PreloadedFragmentKey.EMAIL, facebookEmail);
        }
    }

    public void toNextPage(String dataToStore) {
        setSignupData(mCurrentKey, dataToStore);
        if (mCurrentKey != PreloadedFragmentKey.CAPTCHA && (getCurrentSignupType() != SignupType.FACEBOOK && mCurrentKey != PreloadedFragmentKey.PASSWORD)) {
            triggerSetLoadingListener(mCurrentKey);
        }
        if (mIsPopbakckForInput && mCurrentKey != PreloadedFragmentKey.CAPTCHA) {
            if (getCurrentSignupType() != SignupType.FACEBOOK) {
                showPreloadedFragment(PreloadedFragmentKey.CAPTCHA, false);
            } else {
                validateCountrybeforeCreateNewUser();
            }
        } else {
            switch (mCurrentKey) {
                case USERNAME:
                    /** get country code by ip from server **/
                    if (TextUtils.isEmpty(NetworkUtils.getCountryCodeBySim(ApplicationEx.getContext()))) {
                        NetworkUtils.getCountryCodeByIp(getGetCountryCodeListener());
                    }
                    String facebookEmail = FacebookLoginController.getInstance().getFacebookEmail();
                    if (getCurrentSignupType() != SignupType.FACEBOOK || (getCurrentSignupType() == SignupType.FACEBOOK && TextUtils.isEmpty(facebookEmail))) {
                        showPreloadedFragment(PreloadedFragmentKey.EMAIL, false);
                    } else if (getCurrentSignupType() == SignupType.FACEBOOK) {
                        showPreloadedFragment(PreloadedFragmentKey.PASSWORD, false);
                    }
                    break;
                case EMAIL:
                    showPreloadedFragment(PreloadedFragmentKey.PASSWORD, false);
                    break;
                case PASSWORD:
                    if (getCurrentSignupType() == SignupType.FACEBOOK) {
                        validateCountrybeforeCreateNewUser();
                    } else {
                        showPreloadedFragment(PreloadedFragmentKey.CAPTCHA, false);
                    }
                    break;
                case CAPTCHA:
                    validateCountrybeforeCreateNewUser();
                    break;
            }
        }
    }

    public void checkUserName(String username, GetSuggestUsersListener getSuggestUsersListener) {
        setGAMonitorStartTime();
        ApplicationEx.getInstance().getRequestManager().getSuggestUsers(username, getSuggestUsersListener);
    }

    public void checkEmail(String email, CheckEmailValidationListener checkEmailValidationListener) {
        setGAMonitorStartTime();
        ApplicationEx.getInstance().getRequestManager().checkEmailValidation(email, checkEmailValidationListener);
    }

    public void resendEmail() {
        GAEvent.Signup_ComfirmResendEmail.send(Version.getVasTrackingId());
        String username = getDataFromSignupSharePreference(PreloadedFragmentKey.USERNAME.toString());
        String email = getDataFromSignupSharePreference(PreloadedFragmentKey.EMAIL.toString());
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email)) {
            initialResendEmailListener();
            setGAMonitorStartTime();
            ApplicationEx.getInstance().getRequestManager().resendVerificationEmail(username, email, mResendEmailListener);
        }
    }

    public void initialResendEmailListener() {
        mResendEmailListener = new ResendEmailListener() {
            @Override
            public void onEmailResent(boolean result) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        long interval = calculateResponsePeriod();
                        if (interval > 0) {
                            GAEvent.Signup_Timing_Resend_email.sendTiming(Version.getVasTrackingId(), interval);
                        }
                        Toast.makeText(LoginActivity.this, I18n.tr("We have sent a new verification email!"), Toast.LENGTH_SHORT).show();
                        Logger.info.log(LOG_TAG, "We have sent a new verification email!");
                    }
                });
            }

            @Override
            public void onError(final MigError.Type errType, final long errno, final String errMsg) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        long interval = calculateResponsePeriod();
                        if (interval > 0) {
                            GAEvent.Signup_Timing_Resend_email.sendTiming(Version.getVasTrackingId(), interval);
                        }
                        switch (errType) {
                            case HTTP_REQUEST_TIMEOUT:
                                GAEvent.Signup_API_TimeOut.send(Version.getVasTrackingId());
                                Toast.makeText(LoginActivity.this, I18n.tr("Connection timeout, please try again later!"), Toast.LENGTH_SHORT).show();
                                break;
                            case HTTP_WORKER_ERROR:
                                Toast.makeText(LoginActivity.this, I18n.tr("Cannot connect to server."), Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;

                        }
                    }
                });
            }
        };
    }

    public void validateRegisterToken() {
        String registrationToken = getIntent().getStringExtra(REGISTRATION_KEY);
        initialVerifyRegisterTokenListener();
        String registrationIp = NetworkUtils.getLocalIpAddress(this);
        if (!TextUtils.isEmpty(registrationToken)) {
            if (ApplicationEx.getInstance() != null) {
                if (ApplicationEx.getInstance().getRequestManager() != null) {
                    setGAMonitorStartTime();
                    ApplicationEx.getInstance().getRequestManager().verifyRegisterToken(registrationToken, registrationIp, mVerifyRegisterTokenListener);
                }
            }
        }
    }

    public void initialVerifyRegisterTokenListener() {
        mVerifyRegisterTokenListener = new VerifyRegisterTokenListener() {
            @Override
            public void onUserVerified(boolean result) {
                if (result) {
                    long interval = calculateResponsePeriod();
                    if (interval > 0) {
                        GAEvent.Signup_Timing_Verifying.sendTiming(Version.getVasTrackingId(), interval);
                    }
                    setVerifyCoditionFromSharePreference(false);
                    showPreloadedFragment(PreloadedFragmentKey.TOKEN_SUCCESS);
                    if (mIsSecondAttapmtToVerifyToken) {
                        GAEvent.Signup_Verify_FailRetrySuccess.send(Version.getVasTrackingId());
                        setIsSecondAttapmtToVerifyToken(false);
                    }
                }
            }

            @Override
            public void onError(final MigError.Type errType, final long errno, final String errMsg) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        long interval = calculateResponsePeriod();
                        if (interval > 0) {
                            GAEvent.Signup_Timing_Verifying.sendTiming(Version.getVasTrackingId(), interval);
                        }
                        switch (errType) {
                            case HTTP_REQUEST_TIMEOUT:
                                GAEvent.Signup_API_TimeOut.send(Version.getVasTrackingId());
                                Toast.makeText(LoginActivity.this, I18n.tr("Connection timeout, please try again later!"), Toast.LENGTH_SHORT).show();
                                showPreloadedFragment(PreloadedFragmentKey.TOKEN_TIMEOUT);
                                break;
                            case HTTP_WORKER_ERROR:
                                Toast.makeText(LoginActivity.this, I18n.tr("Cannot connect to server."), Toast.LENGTH_SHORT).show();
                                showPreloadedFragment(PreloadedFragmentKey.TOKEN_TIMEOUT);
                                break;
                            default:
                                if (errMsg.contains(mTempDuplicateKey)) {
                                    showPreloadedFragment(PreloadedFragmentKey.TOKEN_USED);
                                } else {
                                    if (mIsSecondAttapmtToVerifyToken) {
                                        GAEvent.Signup_Verify_FailRetryFailure.send(Version.getVasTrackingId());
                                    }
                                    showPreloadedFragment(PreloadedFragmentKey.TOKEN_EXPIRED);
                                }
                                break;

                        }
                    }
                });

            }

        };
    }

    public void setIsSecondAttapmtToVerifyToken(boolean isRetry) {
        mIsSecondAttapmtToVerifyToken = isRetry;
    }

    private void validateCountrybeforeCreateNewUser() {
        boolean hasSimCode = !TextUtils.isEmpty(NetworkUtils.getCountryCodeBySim(ApplicationEx.getContext()));
        if ((!hasSimCode && mIsGetCountryCodeReturn) || hasSimCode) {
            if (!createNewUser(NetworkUtils.getCountryCode(ApplicationEx.getContext()))) {
                Toast.makeText(ApplicationEx.getContext(), I18n.tr("Cannot identify your country."), Toast.LENGTH_SHORT).show();
                triggerSetLoadingListener(mCurrentKey);
            }
        } else {
            Toast.makeText(ApplicationEx.getContext(), I18n.tr("Oops! something went wrong, please try again!"), Toast.LENGTH_LONG).show();
            triggerSetLoadingListener(mCurrentKey);
        }
    }

    private boolean createNewUser(String countryCode) {
        boolean returnBoolean = false;
        String userName = mSignupBundle.getString(PreloadedFragmentKey.USERNAME.toString());
        String email = mSignupBundle.getString(PreloadedFragmentKey.EMAIL.toString());
        String password = mSignupBundle.getString(PreloadedFragmentKey.PASSWORD.toString());
        String registrationIp = NetworkUtils.getLocalIpAddress(this);
        String captchaSolution = mSignupBundle.getString(PreloadedFragmentKey.CAPTCHA.toString());
        String registrationType;
        String FbAccessToken = "";
        String FBUserId = "";
        if (getCurrentSignupType() == SignupType.FACEBOOK) {
            FbAccessToken = FacebookLoginController.getInstance().getAccessToken();
            FBUserId = FacebookLoginController.getInstance().getFacebookId();
        }
        if (!TextUtils.isEmpty(FbAccessToken) && !TextUtils.isEmpty(FBUserId)) {
            registrationType = SIGNUP_REGISTRACTION_TYPE_FACEBOOK_KEY;
        } else {
            registrationType = SIGNUP_REGISTRACTION_TYPE_NORMAL_KEY;
        }
        if (!TextUtils.isEmpty(countryCode)) {
            setGAMonitorStartTime();
            ApplicationEx.getInstance().getRequestManager().createNewUser(email, FBUserId, FbAccessToken, userName, password,
                    countryCode, registrationIp, getCaptchaSession(),
                    captchaSolution, registrationType, mCreateNewUserListener);
            returnBoolean = true;
        }
        return returnBoolean;
    }

    public void setCaptchaSession(String session) {
        mCurrentCaptchaSession = session;
    }

    private String getCaptchaSession() {
        return mCurrentCaptchaSession;
    }

    public boolean isInPopbackForNewInputCondition() {
        return mIsPopbakckForInput;
    }

    private void popbackForNewInput(PreloadedFragmentKey key) {
        enablePopbackForNewInputStatus();
        triggerErrorSetBannerListener(key);
        triggerSetRegenCaptchaListener(PreloadedFragmentKey.CAPTCHA);
        showPreloadedFragment(key, true);
    }

    public void setErrorBannerListener(String key, SetBannerListener listener) {
        mSetBannerListeners.put(key, listener);
    }

    public void setOnLoadWebListener(OnLoadWebListener listener) {
        mOnLoadWebListeners.add(listener);
    }

    public void setRegeneratCaptchaListener(String key, RegenerateCaptchaListener listener) {
        mSetRegenCaptchaListeners.put(key, listener);
    }

    public void setCleanFieldListener(String key, CleanAllFieldsListener listener) {
        mSetCleanAllFieldsListeners.put(key, listener);
    }

    public void setEmailListener(String key, SetSuccessEmailListener listener) {
        mSetEmailListeners.put(key, listener);
    }

    public void setShowLoadingListener(String key, SetShowLoadingListener listener) {
        mSetLoadingListeners.put(key, listener);
    }

    private void triggerSetLoadingListener(final PreloadedFragmentKey key) {
        if (mSetLoadingListeners.containsKey(key.toString())) {
            mSetLoadingListeners.get(key.toString()).onSetLoading(false);
        }
    }

    private void triggerSetEmailListener(final PreloadedFragmentKey key) {
        if (mSetEmailListeners.containsKey(key.toString())) {
            mSetEmailListeners.get(key.toString()).onSetEmail();
        }
    }

    private void triggerSetRegenCaptchaListener(final PreloadedFragmentKey key) {
        if (mSetRegenCaptchaListeners.containsKey(key.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mSetRegenCaptchaListeners.get(key.toString()).onRegenerate();
                }
            });
        }
    }

    private void triggerErrorSetBannerListener(final PreloadedFragmentKey key) {
        if (mSetBannerListeners.containsKey(key.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mSetBannerListeners.get(key.toString()).onSetBanner(BaseSignupFragment.BannerType.ERROR);
                }
            });
        }
    }

    private void triggerResetBannerListener(final PreloadedFragmentKey key) {
        if (mSetBannerListeners.containsKey(key.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mSetBannerListeners.get(key.toString()).onSetBanner(BaseSignupFragment.BannerType.NORMAL);
                }
            });
        }
    }

    private void triggerResetAllBannerListener() {
        final Iterator iterator = mSetBannerListeners.keySet().iterator();
        while (iterator.hasNext()) {
            final String tempKey = iterator.next().toString();
            if (mSetBannerListeners.containsKey(tempKey)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mSetBannerListeners.get(tempKey).onSetBanner(BaseSignupFragment.BannerType.NORMAL);
                    }
                });
            }
        }
    }

    private void triggerCleanSignupInputFieldListener(final PreloadedFragmentKey key) {
        if (mSetCleanAllFieldsListeners.containsKey(key.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mSetCleanAllFieldsListeners.get(key.toString()).onFieldClean();
                }
            });
        }
    }

    private void triggerCleanAllSignupInputFieldsListener() {
        final Iterator iterator = mSetCleanAllFieldsListeners.keySet().iterator();
        while (iterator.hasNext()) {
            final String tempKey = iterator.next().toString();
            if (mSetCleanAllFieldsListeners.containsKey(tempKey)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mSetCleanAllFieldsListeners.get(tempKey).onFieldClean();
                    }
                });
            }
        }
    }

    private void triggerOnLoadWebListener() {
        for (final OnLoadWebListener listener : mOnLoadWebListeners) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listener.onLoadWeb();
                }
            });
        }
    }

    private void showDialog(DialogType type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        switch (type) {
            case BACKPRESSED:
                dialog.setMessage(I18n.tr("Are you sure you want to leave and cancel this registration?"));
                dialog.setPositiveButton(I18n.tr("Yes"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        abortCurrentSignup();
                        if (mCurrentKey == PreloadedFragmentKey.USERNAME) {
                            GAEvent.Signup_ErrorHandle_UsernameTerminate.send(Version.getVasTrackingId());
                        } else if (mCurrentKey == PreloadedFragmentKey.EMAIL) {
                            GAEvent.Signup_ErrorHandle_EmailTerminate.send(Version.getVasTrackingId());
                        }
                    }
                });
                dialog.setNegativeButton(I18n.tr("No"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
            case ALERT:
                dialog.setMessage(I18n.tr("Sorry! You’ve tried to register too many times in one hour, please come back and try again later."));
                dialog.setPositiveButton(I18n.tr("OK"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                break;
        }
        dialog.show();
    }


    private GetLocationCountryListener getGetCountryCodeListener() {
        return mGetLocationCountryListener;
    }

    private void setCleanFailAttemptCount() {
        mIncorrectCaptchaAttemptCount = 0;
        mIncorrectUsernameAttemptCount = 0;
        mIncorrectPasswordAttemptCount = 0;
    }

    public void abortCurrentSignup() {
        resetSignupSharePreference();
        disablePopbackForNewInputStatus();
        triggerResetAllBannerListener();
        showPreloadedFragment(PreloadedFragmentKey.LOGIN);
    }

    public void resetSignupSharePreference() {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                mSignupSharePreferencesKey, Context.MODE_PRIVATE);
        sharedPref.edit().clear().commit();
    }

    public boolean SaveToSignupSharePreference() {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                mSignupSharePreferencesKey, Context.MODE_PRIVATE);
        String usernameKey = PreloadedFragmentKey.USERNAME.toString();
        String emailKey = PreloadedFragmentKey.EMAIL.toString();
        sharedPref.edit().putString(usernameKey, mSignupBundle.getString(usernameKey)).putString(emailKey, mSignupBundle.getString(emailKey)).commit();
        return (sharedPref.contains(usernameKey) && sharedPref.contains(usernameKey));
    }

    public String getDataFromSignupSharePreference(String key) {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                mSignupSharePreferencesKey, Context.MODE_PRIVATE);
        String bundledEmail = "";
        if (mSignupBundle != null) {
            bundledEmail = mSignupBundle.getString(PreloadedFragmentKey.EMAIL.toString());
        }
        return sharedPref.getString(key, bundledEmail);
    }

    public void setVerifyCoditionFromSharePreference(boolean toBlock) {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                mSignupSharePreferencesKey, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(mSignupSharePreferencesBlockConditionKey, toBlock).commit();
    }

    public boolean getVerifyConditionFromSharePreference() {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                mSignupSharePreferencesKey, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(mSignupSharePreferencesBlockConditionKey, false);
    }

    public void setGAMonitorStartTime() {
        mResponseStartTime = System.currentTimeMillis();
    }

    public long calculateResponsePeriod() {
        long endTime = System.currentTimeMillis();
        long period = 0;
        if (mResponseStartTime > 0) {
            period = endTime - mResponseStartTime;
            if (period > MigRequest.DEFAULT_TIMEOUT) {
                period = MigRequest.DEFAULT_TIMEOUT;
            }
            mResponseStartTime = 0;
        }
        return period;
    }
}
