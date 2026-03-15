package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Version;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.CreateCaptchaSessionListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.listener.RegenerateCaptchaListener;

/*
 * Created by justinhsu on 4/8/15.
 */
public class SignupVerifyFragment extends BaseSignupFragment implements RegenerateCaptchaListener {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup_create_verify;
    }

    enum CaptchaType {SHOW, HIDE, LOADING}

    private EditText                        mCode;
    private TextView                        mSlognText;
    private TextView                        mCaptchaHint;
    private TextView                        mTitleText;
    private ProgressBar                     mCaptchaLoading;
    private ImageView                       mSlognView;
    private RelativeLayout                  mSlognlayout;
    private ImageButton                     mNextButton;
    private ImageButton                     mPreviousButton;
    private ImageView                       mCaptcha;
    private ImageButton                     mResetCaptchaButton;
    private ProgressBar                     mNextStepProgress;
    private CreateCaptchaSessionListener    mCaptchaSessionListener;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCode = (EditText) view.findViewById(R.id.signup_verify);
        mCode.requestFocus();
        mCaptcha = (ImageView) view.findViewById(R.id.captcha);
        mCaptchaLoading = (ProgressBar) view.findViewById(R.id.captcha_loading_progressbar);
        mCaptchaHint = (TextView) view.findViewById(R.id.captcha_hint);
        mResetCaptchaButton = (ImageButton) view.findViewById(R.id.reset_captcha);
        mResetCaptchaButton.setOnClickListener(this);
        mNextStepProgress = (ProgressBar) view.findViewById(R.id.progressbar);
        mSlognlayout = (RelativeLayout) view.findViewById(R.id.slogan_container);
        mSlognText = (TextView) view.findViewById(R.id.pick_verify_slogan);
        mTitleText = (TextView) view.findViewById(R.id.title);
        mSlognView = (ImageView) view.findViewById(R.id.pick_verify_image);
        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) view.findViewById(R.id.previousButton);
        mPreviousButton.setOnClickListener(this);
        mCode.setOnKeyListener(this);
        mNextButton.setOnClickListener(this);
        mTitleText.setText(I18n.tr("Verify Code"));
        mCode.setHint(I18n.tr("Code"));
        mCaptchaSessionListener = new CreateCaptchaSessionListener() {
            @Override
            public void onCaptchaSession(final String session, final String captchaImageUrl) {
                final LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            long interval = activity.calculateResponsePeriod();
                            if (interval > 0) {
                                GAEvent.Signup_Timing_Captcha.sendTiming(Version.getVasTrackingId(), interval);
                            }
                            setCaptcha(session, captchaImageUrl);
                        }
                    });
                }
            }

            @Override
            public void onError(final MigError.Type errType, final long errno, final String errMsg) {
                final LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            long interval = activity.calculateResponsePeriod();
                            if (interval > 0) {
                                GAEvent.Signup_Timing_Captcha.sendTiming(Version.getVasTrackingId(), interval);
                            }
                            showCpatcha(CaptchaType.HIDE);
                            showCaptchaResetButton(true);
                            switch (errType) {
                                case HTTP_REQUEST_TIMEOUT:
                                    toShowLoading(false);
                                    Toast.makeText(activity, I18n.tr("Connection timeout, please try again later!"), Toast.LENGTH_SHORT).show();
                                    break;
                                case HTTP_WORKER_ERROR:
                                    toShowLoading(false);
                                    Toast.makeText(activity, I18n.tr("Cannot connect to server"), Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    toShowLoading(false);
                                    Toast.makeText(activity, I18n.tr("Server error."), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                }
            }
        };
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.CAPTCHA.toString();
        super.onViewCreated(view, savedInstanceState);
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.setRegeneratCaptchaListener(LoginActivity.PreloadedFragmentKey.CAPTCHA.toString(), this);
            generateCaptcha();
            activity.addOnFragmentShowListener(LoginActivity.PreloadedFragmentKey.CAPTCHA, new LoginActivity.OnFragmentShowListener() {
                @Override
                public void show() {
                    generateCaptcha();
                    mCode.requestFocus();
                }
            });

        }

        addButtonToNoConnectionDisableButtonList(mNextButton);

    }

    public void generateCaptcha() {
        showCaptchaResetButton(true);
        final LoginActivity activity = ((LoginActivity) getActivity());
        final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (activity != null && requestManager != null) {
            activity.setGAMonitorStartTime();
            ApplicationEx.getInstance().getRequestManager().createCaptchaSession(mCaptchaSessionListener);
            showCpatcha(CaptchaType.LOADING);
        }
    }

    private void showCpatcha(CaptchaType type) {
        switch (type) {
            case SHOW:
                mCaptchaLoading.setVisibility(View.INVISIBLE);
                mCaptcha.setVisibility(View.VISIBLE);
                break;
            case HIDE:
                mCaptcha.setImageResource(android.R.color.transparent);
                mCaptchaLoading.setVisibility(View.INVISIBLE);
                break;
            case LOADING:
                mCaptcha.setImageResource(android.R.color.transparent);
                mCaptchaLoading.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setCaptcha(final String session, final String url) {
        if (!TextUtils.isEmpty(url)) {
            ImageHandler.getInstance().loadImage(url, mCaptcha);
            showCpatcha(CaptchaType.SHOW);
        }
        if (!TextUtils.isEmpty(session)) {
            final LoginActivity activity = ((LoginActivity) getActivity());
            if (activity != null) {
                activity.setCaptchaSession(session);
            }
        }
    }

    @Override
    public void setBannerResource(final String slognText, final int slognLayout, final int slognView, final String hint, final int hintColor) {
        mSlognText.setText(slognText);
        mSlognlayout.setBackgroundColor(getResources().getColor(slognLayout));
        mSlognView.setImageResource(slognView);
        mCaptchaHint.setText(hint);
        mCaptchaHint.setTextColor(getResources().getColor(hintColor));
    }

    @Override
    public void setBanner(BannerType type) {
        switch (type) {
            case NORMAL:
            case SUCCESS:
                setBannerResource(I18n.tr("Enter text shown"), R.color.signup_normal, R.drawable.ad_purplebot_detective, "", R.color.white_text_color);
                mCaptchaHint.setVisibility(View.GONE);
                break;
            case ERROR:
            case RETURN_ERROR:
                setBannerResource(I18n.tr("C'mon, you can do better than this!"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("Oops, wrong code. Please enter code again."), R.color.signup_text_red);
                mCaptchaHint.setVisibility(View.VISIBLE);
                break;
        }
    }

    public String getInput() {
        return mCode.getText().toString();
    }

    @Override
    protected boolean validateInputWithRegularExpress(String input) {
        if (input.length() > 0) {
            setBanner(BannerType.SUCCESS);
            return true;
        } else {
            setBanner(BannerType.NORMAL);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int viewId = view.getId();
        switch (viewId) {
            case R.id.reset_captcha:
                GAEvent.SignUp_CaptchaRefresh.send(Version.getVasTrackingId());
                cleanCaptchaField();
                generateCaptcha();
                break;
        }
    }

    @Override
    protected void validateInputWithServer(String input) {
        super.validateInputWithServer(input);
        showCaptchaResetButton(false);
        toNextPage(input);
    }

    private void showCaptchaResetButton(boolean toShow) {
        if (toShow) {
            mResetCaptchaButton.setVisibility(View.VISIBLE);
        } else {
            mResetCaptchaButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void toShowLoading(final boolean isShow) {
        if (mNextStepProgress == null || mNextButton == null) {
            return;
        }
        if (isShow) {
            mNextStepProgress.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.INVISIBLE);
        } else {
            mNextStepProgress.setVisibility(View.GONE);
            mNextButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRegenerate() {
        generateCaptcha();
    }

    @Override
    public void onResume() {
        super.onResume();
        generateCaptcha();
        cleanCaptchaField();
    }

    @Override
    public void onSetLoading(boolean isShow) {
        super.onSetLoading(isShow);
        showCaptchaResetButton(true);
    }

    private void cleanCaptchaField() {
        if (mCode != null) {
            mCode.setText("");
        }
    }

    @Override
    public void onFieldClean() {
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null && mCode != null) {
            mCode.setText(activity.getSignupData(LoginActivity.PreloadedFragmentKey.CAPTCHA));
            mCode.requestFocus();
        }
    }
}