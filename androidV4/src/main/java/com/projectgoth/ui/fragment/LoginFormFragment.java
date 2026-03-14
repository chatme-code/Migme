/**
 * Copyright (c) 2013 Project Goth
 *
 * LoginFormFragment.java.java
 * Created May 30, 2013, 12:25:01 AM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.fusion.packet.FusionPktError;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.common.Constants;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Tools;
import com.projectgoth.common.Version;
import com.projectgoth.controller.SystemController;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.listeners.LoginCaptchaListener;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.widget.DateTextView;
import com.projectgoth.util.FiksuInterface;
import com.projectgoth.util.FiksuInterface.OneTimeEvent;
import com.projectgoth.util.FiksuInterface.RecurringEvent;

/**
 * @author cherryv
 * 
 */
public class LoginFormFragment extends BaseFragment implements OnClickListener, OnFocusChangeListener, TextWatcher, LoginCaptchaListener, TextView.OnEditorActionListener {

    private EditText             mTxtUsername;
    private EditText             mTxtPassword;
    private ImageButton          mBtnSignin;
    private ImageButton          mBtnSigninCaptcha;
    private Button               mBtnFBConnect;
    private Button               mSignupResendButton;
    private Button               mBtnToSignUp;
    private Button               mBtnToSignin;
    private Button               mBtnForgotPassword;
    private Button               mBtnShowHidePassword;
    private DateTextView         mOrLineText;
    private TextView             mTxtBot;
    private ImageView            mImgBot;
    private EditText             mTxtCaptcha;
    private ImageView            mImgCaptcha;
    private Button               mBtnDiscover;
    private ProgressBar          mProgressBar;
    private ProgressBar          mProgressBarCaptcha;
    private View                 mDisableMask;
    private View                 mLineUserPwd;
    private View                 mLineBelowPwd;
    private View                 mPaddingAboveDiscover;
    private RelativeLayout       mBotLayout;
    private RelativeLayout       mCaptchaLayout;
    private RelativeLayout       mResendLayout;
    private RelativeLayout       mSigninInputLayout;
    private RelativeLayout       mTermsOfUseLayout;
    private RelativeLayout       mPrivacyPolicyLayout;
    private ImageView            mImgTriangleIndicator;
    private TextView             mTxtTermsOfUse;
    private TextView             mTermOfUsePhrase;
    private TextView             mTxtPrivacyPolicy;
    private TextView             mPrivacyPolicyPhrase;
    private String               mCurrentUsername           = Constants.BLANKSTR;
    private boolean              mIsCaptchaLogin            = false;
    private TextView             mTextHint;
    private static final int     VALID_PASSWORD_LENGTH      = 6;
    private static final String  PASSWORD_HASH              = "**";
    private TextWatcher          mPasswordTextWatcher;
    private SharedPrefsManager   mSharedPrefsManager        = ApplicationEx.getInstance().getSharedPrefsManager();
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login_form_new;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SystemController.getInstance().setLoginCaptchaListener(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SystemController.getInstance().setLoginCaptchaListener(this);

        mBotLayout = (RelativeLayout) view.findViewById(R.id.bot_layout);
        mCaptchaLayout = (RelativeLayout) view.findViewById(R.id.captcha_layout);
        mTermsOfUseLayout = (RelativeLayout) view.findViewById(R.id.terms_of_use_layout);
        mPrivacyPolicyLayout = (RelativeLayout) view.findViewById(R.id.privacy_policy_layout);
        mTextHint = (TextView) view.findViewById(R.id.txt_enter_hint);
        mTextHint.setText(I18n.tr("Enter text shown below"));
        mPaddingAboveDiscover = (View) view.findViewById(R.id.padding_above_discover);
        mSignupResendButton = (Button) view.findViewById(R.id.signupResendButton);
        mSignupResendButton.setOnClickListener(this);
        mSignupResendButton.setText(I18n.tr("Resend email"));
        mTxtUsername = (EditText) view.findViewById(R.id.txt_username);
        mTxtUsername.setText(Session.getInstance().getUsername());
        mTxtUsername.setOnClickListener(this);
        mTxtUsername.addTextChangedListener(this);
        mTxtUsername.setHint(I18n.tr("Username"));
        mBtnToSignin = (Button) view.findViewById(R.id.btn_to_login_view);
        mBtnToSignin.setText(I18n.tr("Log in"));
        mBtnToSignUp = (Button) view.findViewById(R.id.login_btn_signup);
        mBtnToSignUp.setText(I18n.tr("Sign up"));
        mBtnToSignUp.setOnClickListener((LoginActivity) getActivity());
        mTxtPassword = (EditText) view.findViewById(R.id.txt_password);
        mTxtPassword.setOnFocusChangeListener(this);
        mTxtPassword.setText(maskStoredPassword());
        mTxtPassword.setOnClickListener(this);
        mTxtPassword.addTextChangedListener(this);
        mTxtPassword.setHint(I18n.tr("Password"));
        mTxtPassword.setOnEditorActionListener(this);

        mResendLayout = (RelativeLayout) view.findViewById(R.id.signup_resend_layout);
        mResendLayout.setVisibility(View.GONE);
        mSigninInputLayout = (RelativeLayout) view.findViewById(R.id.signin_input_layout);
        mTxtTermsOfUse = (TextView) view.findViewById(R.id.txt_terms_of_use_link);
        mTxtTermsOfUse.setText(I18n.tr("terms of use."));
        mTermOfUsePhrase = (TextView) view.findViewById(R.id.txt_terms_of_use);
        mTermOfUsePhrase.setText(I18n.tr("By signing up, you agree to our "));
        mTxtPrivacyPolicy = (TextView) view.findViewById(R.id.txt_privacy_policy_link);
        mTxtPrivacyPolicy.setText(I18n.tr("privacy policy."));
        mPrivacyPolicyPhrase = (TextView) view.findViewById(R.id.txt_privacy_policy);
        mPrivacyPolicyPhrase.setText(I18n.tr("View "));
        mTxtTermsOfUse.setOnClickListener((LoginActivity) getActivity());
        mTxtPrivacyPolicy.setOnClickListener((LoginActivity) getActivity());

        mLineUserPwd = (View) view.findViewById(R.id.username_password_separator);
        mLineBelowPwd = (View) view.findViewById(R.id.below_password_separator);

        mTxtBot = (TextView) view.findViewById(R.id.txt_bot);
        mTxtBot.setText(I18n.tr("Welcome back"));
        mTxtCaptcha = (EditText) view.findViewById(R.id.txt_captcha);
        mTxtCaptcha.setHint(I18n.tr("Code"));

        mImgBot = (ImageView) view.findViewById(R.id.img_bot);
        mImgCaptcha = (ImageView) view.findViewById(R.id.img_captcha);
        mImgTriangleIndicator = (ImageView) view.findViewById(R.id.img_fragment_indicator);

        mDisableMask = (View) view.findViewById(R.id.mask);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBarCaptcha = (ProgressBar) view.findViewById(R.id.progress_bar_captcha);

        mBtnSignin = (ImageButton) view.findViewById(R.id.btn_signin);
        mBtnSignin.setOnClickListener(this);

        mBtnSigninCaptcha = (ImageButton) view.findViewById(R.id.btn_signin_captcha);
        mBtnSigninCaptcha.setOnClickListener(this);

        mBtnForgotPassword = (Button) view.findViewById(R.id.btn_forgotpassword);
        mBtnForgotPassword.setText(I18n.tr("Forget?"));
        mBtnForgotPassword.setOnClickListener((View.OnClickListener) getActivity());

        mBtnShowHidePassword = (Button) view.findViewById(R.id.btn_showhide_password);
        mBtnShowHidePassword.setOnClickListener(this);

        mBtnFBConnect = (Button) view.findViewById(R.id.btn_fbconnect);
        if (UIUtils.hasFroyo()) {
            mBtnFBConnect.setVisibility(View.VISIBLE);
            mBtnFBConnect.setOnClickListener((LoginActivity) getActivity());
        }
        if (mTxtPassword.getText().toString().contains(PASSWORD_HASH)) {
            mPasswordTextWatcher = new TextWatcher() {
                String originText = "";
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    originText = String.valueOf(s);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().contains(PASSWORD_HASH) && count != s.length()) {
                        mTxtPassword.setText(comparePassword(originText, String.valueOf(s)));
                        mTxtPassword.setSelection(mTxtPassword.length());
                        mTxtPassword.removeTextChangedListener(mPasswordTextWatcher);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            mTxtPassword.addTextChangedListener(mPasswordTextWatcher);
        }

        ImageView imgConnSettings = (ImageView) view.findViewById(R.id.img_conn_settings);
        imgConnSettings.setOnClickListener(this);

        mCurrentUsername = Session.getInstance().getUsername();
        mTxtUsername.setText(mCurrentUsername);

        mBtnDiscover = (Button) view.findViewById(R.id.btn_discover);
        mBtnDiscover.setOnClickListener((LoginActivity) getActivity());

        mOrLineText = (DateTextView) view.findViewById(R.id.or_line_text);
        mOrLineText.setText(I18n.tr("Or"));

        addButtonToNoConnectionDisableButtonList(mBtnSignin);

        checkAndEnableSignInButton();
        showHidePassword();
    }


    private String comparePassword(String origin, String edited) {
        String returnStr = "";
        int originLength = origin.length();
        int editedLength = edited.length();
        if (editedLength > originLength) {
            for (int i = 0; i < originLength; i++) {
                String originAtIndex = String.valueOf(origin.charAt(i));
                String editedAtIndex = String.valueOf(edited.charAt(i));
                if (!originAtIndex.equals(editedAtIndex)) {
                    returnStr = editedAtIndex;
                    break;
                }
            }
            if (TextUtils.isEmpty(returnStr)) {
                returnStr = String.valueOf(edited.charAt(editedLength - 1));
            }
        }
        return returnStr;
    }

    private String maskStoredPassword() {
        String password = "";
        String storedPassword = Session.getInstance().getPasswordInLocal();
        StringBuilder storedPasswordBuilder = new StringBuilder(storedPassword == null ? "" : storedPassword);
        if (storedPasswordBuilder.length() >= VALID_PASSWORD_LENGTH) {
            int passwordShownIndex = 2;
            int hashLength = storedPasswordBuilder.length() - passwordShownIndex;
            for (int i = passwordShownIndex; i < hashLength; i++) {
                storedPasswordBuilder.setCharAt(i, '*');
            }
            password = storedPasswordBuilder.toString();
        }
        return password;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        final LoginActivity activity = (LoginActivity) getActivity();
        switch (viewId) {
            case R.id.btn_signin:
                activity.setGAMonitorStartTime();
                if (SystemDatastore.getInstance().isFirstTimeLog(GAEvent.Signin_Click.toString())) {
                    GAEvent.Signin_Click.send(Version.getVasTrackingId());
                }
                NetworkService networkService = ApplicationEx.getInstance().getNetworkService();
                if (!networkService.isNetworkAvailable()) {
                    return;
                }

                mIsCaptchaLogin = false;
                attemptLogin();
                break;
            case R.id.btn_signin_captcha:
                networkService = ApplicationEx.getInstance().getNetworkService();
                if (!networkService.isNetworkAvailable()) {
                    return;
                }

                if (mTxtCaptcha != null && !TextUtils.isEmpty(mTxtCaptcha.getText().toString())) {
                    mIsCaptchaLogin = true;
                    String captchaInput = mTxtCaptcha.getText().toString();
                    SystemController.getInstance().sendCaptchaResponse(captchaInput);
                    attemptLogin();
                }
                break;
            case R.id.txt_username:
                break;
            case R.id.txt_password:
                break;
            case R.id.img_conn_settings:
                ActionHandler.getInstance().displayConnectionSettings(activity);
                break;
            case R.id.btn_showhide_password:
                if (SystemDatastore.getInstance().isFirstTimeLog(GAEvent.Signin_ShowPassword.toString())) {
                    GAEvent.Signin_ShowPassword.send(Version.getVasTrackingId());
                }
                showHidePassword();
                break;
            case R.id.signupResendButton:
                activity.resendEmail();
                showResendButton(false);
                break;
        }
    }

    public void showHidePassword() {
        if ((mBtnShowHidePassword.getText().toString()).equals(I18n.tr("Show Password"))) {
            mBtnShowHidePassword.setText(I18n.tr("Hide Password"));
            mTxtPassword.setTransformationMethod(null);
            mTxtPassword.setSelection(mTxtPassword.length());
        } else {
            mBtnShowHidePassword.setText(I18n.tr("Show Password"));
            mTxtPassword.setTransformationMethod(new PasswordTransformationMethod());
            mTxtPassword.setSelection(mTxtPassword.length());
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Login.SUCCESS);
        registerEvent(Events.Login.ERROR);
        registerEvent(AppEvents.NetworkService.DISCONNECTED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final LoginActivity activity = (LoginActivity) getActivity();
        String action = intent.getAction();
        if (action.equals(Events.Login.SUCCESS)) {
            GAEvent.Signin_Success.send(Version.getVasTrackingId());
            long interval = activity.calculateResponsePeriod();
            if (interval > 0) {
                GAEvent.Signin_Timing_Login.sendTiming(Version.getVasTrackingId(), interval);
            }
            onLoginSuccess();
        } else if (action.equals(Events.Login.ERROR)) {
            short errorCode = intent.getShortExtra(Events.Misc.Extra.ERROR_TYPE, FusionPktError.ErrorType.UNDEFINED.getValue());
            GAEvent.Signin_Failure.sendWithErrorCode(Version.getVasTrackingId(), errorCode);
            String message = intent.getStringExtra(Events.Misc.Extra.ERROR_MESSAGE);
            onLoginFailed(message);
            if (errorCode != FusionPktError.ErrorType.INVALID_CREDENTIALS.getValue() &&
                    errorCode != FusionPktError.ErrorType.UNDEFINED.getValue()) {
                Tools.showToast(activity, I18n.tr("unexpected error happened, please try again later!"));
            }
        }
    }

    private boolean attemptLogin() {
        if (mTxtUsername != null &&
                mTxtPassword != null) {
            final String username = mTxtUsername.getText().toString();
            String password;

            //Prevent asterisks from being considered as a part of password, which will cause fail to re-login after signing out.
            //May have a better solution.
            String pwd = mTxtPassword.getText().toString();
            String previousCorrectPassword = Session.getInstance().getPasswordInLocal();
            if (pwd.contains("*") && previousCorrectPassword != null && pwd.length() == previousCorrectPassword.length()) {
                password = Session.getInstance().getPasswordInLocal();
            } else {
                password = pwd;
            }

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                ActionHandler.getInstance().startLogin(username, password, true);
                onLoginStarted();
                return true;
            }
        }
        return false;
    }

    private void onLoginStarted() {
        Tools.hideVirtualKeyboard(getActivity());
        showProgressBar();
        disableClickableViews();
    }

    private void onLoginSuccess() {
        // Fiksu: check if the user is logging in for the first time
        final LoginActivity activity = (LoginActivity) getActivity();
        if (Session.getInstance().getIsFirstTimeUserLogin() &&
                Session.getInstance().isNewlyRegisteredUser()) {
            FiksuInterface.sendEvent(OneTimeEvent.FirstLogin, activity);
        }
        if (Session.getInstance().isFacebookSession()) {
            FiksuInterface.sendEvent(RecurringEvent.Registration_FacebookButton, ApplicationEx.getInstance().getCurrentActivity());

        }
        mTxtBot.setText(I18n.tr("Welcome back"));
        dismissProgressBar();
        enableClickableViews();
        mBotLayout.setBackgroundColor(getResources().getColor(R.color.login_bot_background_default));
        mLineUserPwd.setBackgroundColor(getResources().getColor(R.color.login_line_background));
        mLineBelowPwd.setBackgroundColor(getResources().getColor(R.color.login_line_background));
        mImgBot.setImageResource(R.drawable.ad_login_bot_purplehi);
        mImgTriangleIndicator.setImageResource(R.drawable.ad_triangle_green);
        Session.getInstance().savePasswordToLocal();
        ActionHandler.getInstance().showMainActivityAfterLogin(activity);
    }

    private void onLoginFailed(String message) {
        //TODO: show resend button is disabled due to we can't get user's email address
//        if (message.contains("activate") && message.contains("account")) {
//            //TODO: show resend button is disabled due to we can't get user's email address
//            showResendButton(true);
//        }
        Session.getInstance().clearPasswordInLocal();
        dismissProgressBar();
        enableClickableViews();
        showErrorMessage(message);
    }

    private void showErrorMessage(final String message) {
        if (!TextUtils.isEmpty(message) && mTxtBot != null) {
            mTxtBot.setText(I18n.tr(message));
        }

        mImgBot.setImageResource(R.drawable.ad_login_bot_purplesigh);

        mLineUserPwd.setBackgroundColor(getResources().getColor(R.color.login_error_red));
        mLineBelowPwd.setBackgroundColor(getResources().getColor(R.color.login_error_red));
    }

    private void checkAndEnableSignInButton() {
        if (mTxtUsername == null ||
                mTxtPassword == null ||
                TextUtils.isEmpty(mTxtUsername.getText().toString()) ||
                TextUtils.isEmpty(mTxtPassword.getText().toString())) {
            mBtnSignin.setEnabled(false);
            mBtnSigninCaptcha.setEnabled(false);
        } else {
            mBtnSignin.setEnabled(true);
            mBtnSigninCaptcha.setEnabled(true);
        }
    }

    private void showProgressBar() {
        if (mIsCaptchaLogin) {
            mBtnSigninCaptcha.setVisibility(View.INVISIBLE);
            mProgressBarCaptcha.setVisibility(View.VISIBLE);
        } else {
            mBtnSignin.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void dismissProgressBar() {
        if (mIsCaptchaLogin) {
            mProgressBarCaptcha.setVisibility(View.GONE);
            mBtnSigninCaptcha.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mBtnSignin.setVisibility(View.VISIBLE);
        }
    }

    private void disableClickableViews() {
        mDisableMask.setVisibility(View.VISIBLE);
        mTxtPassword.setEnabled(false); //prevent triggering login again from imeoptions actionDone
    }

    private void enableClickableViews() {
        mDisableMask.setVisibility(View.GONE);
        mTxtPassword.setEnabled(true);
    }

    /* (non-Javadoc)
     * @see android.view.View.OnFocusChangeListener#onFocusChange(android.view.View, boolean)
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == mTxtPassword && hasFocus) {
            final String newUsername = mTxtUsername.getText().toString();
            if (!newUsername.equals(mCurrentUsername)) {
                mTxtPassword.setText(Constants.BLANKSTR);
                mCurrentUsername = newUsername;
            }
        }
    }

    /* (non-Javadoc)
     * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /* (non-Javadoc)
     * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /* (non-Javadoc)
     * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
     */
    @Override
    public void afterTextChanged(Editable s) {
        checkAndEnableSignInButton();
    }

    //ToDo: Remove this when no more captcha is needed.
    @Override
    public void onCaptchaReceived(Captcha captcha) {
        GAEvent.Signin_Failure_AccountBanned.send(Version.getVasTrackingId());

        final byte[] captchaImgRaw = captcha.getImage();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaptchaLayout.setVisibility(View.VISIBLE);
                if (mImgCaptcha != null && captchaImgRaw != null && captchaImgRaw.length > 0) {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(captchaImgRaw, 0, captchaImgRaw.length);
                    if (bitmap != null) {
                        mImgCaptcha.setImageBitmap(bitmap);
                    }
                }
                dismissProgressBar();
                enableClickableViews();
                mBotLayout.setBackgroundColor(getResources().getColor(R.color.login_error_red));
                mLineBelowPwd.setBackgroundColor(getResources().getColor(R.color.login_error_red));
                mLineUserPwd.setBackgroundColor(getResources().getColor(R.color.login_error_red));
                mImgTriangleIndicator.setImageResource(R.drawable.ad_triangle_red);
                mImgBot.setImageResource(R.drawable.ad_login_bot_yellowfail);
                mTxtBot.setText(I18n.tr("Username and password do not match"));
                mTermsOfUseLayout.setVisibility(View.GONE);
                mPrivacyPolicyLayout.setVisibility(View.GONE);
                mBtnDiscover.setVisibility(View.GONE);
                mPaddingAboveDiscover.setVisibility(View.GONE);
                mBtnSignin.setVisibility(View.GONE);
            }
        });

        SystemController.getInstance().setCaptcha(null);
    }

    protected void showResendButton(final boolean isVisible) {
        if (isVisible) {
            if (mResendLayout.getVisibility() == View.GONE) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                animation.setDuration(2500);
                mResendLayout.startAnimation(animation);
                mSigninInputLayout.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mResendLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        } else {
            if (mResendLayout.getVisibility() == View.VISIBLE) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                animation.setDuration(1500);
                mResendLayout.startAnimation(animation);
                mSigninInputLayout.startAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mResendLayout.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            mBtnSignin.performClick();
            return true;
        }
        return false;
    }
}
