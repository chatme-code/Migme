package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.listener.SetSuccessEmailListener;

/**
 * Created by justinhsu on 4/8/15.
 */
public abstract class BaseSignupVerifyFragment extends BaseFragment implements View.OnClickListener, SetSuccessEmailListener {
    enum VerifiedResultType {
        TOKEN_USED, TOKEN_EXPIRED, TOKEN_INVALID, TOKEN_SUCCESS, TIMEOUT, SUCCESS, VERIFYING, FACEBOOK_SUCCESS, FACEBOOK_FAIL
    }
    private static VerifiedResultType sCurrentType;
    protected String    mCurrentFragmentKey = "";
    Button              mExploreButton;
    ImageView           mLogo;
    TextView            mPhrase;
    TextView            mTitlePhrase;
    TextView            mTitlePhraseTwo;
    TextView            mEmail;
    TextView            mResendButton;
    ProgressBar         mVerifyingProgress;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup_verify_email;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitlePhrase = (TextView) view.findViewById(R.id.signup_email_verify_title_phrase);
        mTitlePhraseTwo = (TextView) view.findViewById(R.id.signup_email_verify_title_phrase_two);
        mLogo = (ImageView) view.findViewById(R.id.signup_email_verify_logo);
        mEmail = (TextView) view.findViewById(R.id.signup_email_verify_email);
        mPhrase = (TextView) view.findViewById(R.id.signup_email_verify_phrase);
        mResendButton = (TextView) view.findViewById(R.id.signup_email_verify_resend_email);
        mResendButton.setTextColor(getResources().getColorStateList(R.color.signup_resend_email_onclick));
        mResendButton.setOnClickListener(this);
        mExploreButton = (Button) view.findViewById(R.id.signup_email_verify_explore_button);
        mExploreButton.setBackgroundColor(getResources().getColor(R.color.signup_peek_bg));
        mExploreButton.setOnClickListener(this);
        mVerifyingProgress = (ProgressBar) view.findViewById(R.id.signup_email_verify_verifying_progressbar);
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.setEmailListener(mCurrentFragmentKey, this);
        }
    }

    @Override
    public void onClick(View view) {
        LoginActivity activity = (LoginActivity) getActivity();
        switch (view.getId()) {
            case R.id.signup_email_verify_resend_email:
                if (activity != null) {
                    if (sCurrentType == VerifiedResultType.SUCCESS || sCurrentType == VerifiedResultType.VERIFYING) {
                        activity.abortCurrentSignup();
                    } else {
                        activity.resendEmail();
                    }
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setPageLayout(VerifiedResultType type) {
        hideProgressbar();
        hideTitlePhraseTwo();
        hideEmailAddress();
        hideResendEmailButton();
        sCurrentType = type;
        switch (type) {
            case FACEBOOK_FAIL:
                setFailTitlePhrase();
                setFailBot();
                setPhrase("Facebook signup failed!", View.VISIBLE);
                setSignupExploreButton();
                break;
            case FACEBOOK_SUCCESS:
                setSuccessTitlePhrase();
                setSuccessBot();
                setPhrase("Facebook signup is successful. Now go have fun!", View.VISIBLE);
                setSuccessExploreButton();
                break;
            case TIMEOUT:
                setFailTitlePhrase();
                setFailBot();
                setPhrase("Trouble connecting to migme.", View.VISIBLE);
                showResendEmailButton();
                showRetryExploreButton();
                break;
            case TOKEN_EXPIRED:
                setFailTitlePhrase();
                setFailBot();
                setPhrase("Your verification link has expired.", View.VISIBLE);
                setSignupExploreButton();
                break;
            case TOKEN_INVALID:
                break;
            case TOKEN_SUCCESS:
                setSuccessTitlePhrase();
                setSuccessBot();
                setPhrase("Email confirmed. Now go have fun!", View.VISIBLE);
                setSuccessExploreButton();
                break;
            case TOKEN_USED:
                setSuccessTitlePhrase();
                setSuccessBot();
                setPhrase("Email already confirmed. Now go have fun!", View.VISIBLE);
                setSuccessExploreButton();
                break;
            case SUCCESS:
                setTitlePhrase("Awesome!", View.VISIBLE);
                setTitlePhraseTwo("You're all signed up.", View.VISIBLE);
                showEmailAddress();
                setLogo(R.drawable.ad_rocket_success, View.VISIBLE);
                setPhrase("Verification email sent to", View.VISIBLE);
                showGiveSingupButton();
                showResendEmailExploreButton();
                break;
            case VERIFYING:
                hideTitlePhrase();
                setLogo(0, View.GONE);
                setPhrase("Please wait while we are verifying your account...", View.VISIBLE);
                hideExploreButton();
                showProgressbar();
                break;
            default:
                break;
        }
    }

    private void showGiveSingupButton() {
        setGiveupSignup(View.VISIBLE);
    }

    private void showProgressbar() {
        setProgressBar(View.VISIBLE);
    }

    private void hideProgressbar() {
        setProgressBar(View.GONE);
    }

    private void hideExploreButton() {
        setExploreButton("", View.GONE);
    }

    private void showResendEmailExploreButton() {
        setExploreButton("RESEND EMAIL", View.VISIBLE);
    }

    private void showRetryExploreButton() {
        setExploreButton("RETRY", View.VISIBLE);
    }

    private void setSignupExploreButton() {
        setExploreButton("SIGNUP", View.VISIBLE);
    }

    private void setSuccessExploreButton() {
        setExploreButton("LET'S GO", View.VISIBLE);
    }

    private void hideResendEmailButton() {
        setResendEmail(View.GONE);
    }

    private void showResendEmailButton() {
        setResendEmail(View.GONE);
    }

    private void hideEmailAddress() {
        setEmailAddress(View.GONE);
    }

    public void showEmailAddress() {
        setEmailAddress(View.VISIBLE);
    }

    private void setSuccessTitlePhrase() {
        setTitlePhrase("Woohoo!", View.VISIBLE);
    }

    private void setFailTitlePhrase() {
        setTitlePhrase("Oops!", View.VISIBLE);
    }

    private void hideTitlePhraseTwo() {
        setTitlePhraseTwo("", View.GONE);
    }

    private void hideTitlePhrase() {
        setTitlePhrase("", View.INVISIBLE);
    }

    private void setTitlePhrase(String titlePhrase, int visibility) {
        mTitlePhrase.setText(I18n.tr(titlePhrase));
        mTitlePhrase.setVisibility(visibility);
    }

    private void setTitlePhraseTwo(String titlePhraseTwo, int visibility) {
        mTitlePhraseTwo.setText(I18n.tr(titlePhraseTwo));
        mTitlePhraseTwo.setVisibility(visibility);
    }

    private void setPhrase(String phrase, int visibility) {
        mPhrase.setText(I18n.tr(phrase));
        mPhrase.setVisibility(visibility);
    }

    private void setEmailAddress(int visibility) {
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            String storedEmail = activity.getDataFromSignupSharePreference(LoginActivity.PreloadedFragmentKey.EMAIL.toString());
            if (!TextUtils.isEmpty(storedEmail)) {
                mEmail.setText(I18n.tr(storedEmail));
                mEmail.setVisibility(visibility);
            }
        }
    }

    private void setFailBot() {
        setLogo(R.drawable.ad_bot_fail, View.VISIBLE);
    }

    private void setSuccessBot() {
        setLogo(R.drawable.ad_bot_celebrate, View.VISIBLE);
    }

    private void setLogo(int logo, int visibility) {
        if (visibility != View.GONE || visibility != View.INVISIBLE) {
            mLogo.setImageResource(logo);
        }
        mLogo.setVisibility(visibility);
    }

    private void setExploreButton(String exploreLabel, int visibility) {
        mExploreButton.setText(I18n.tr(exploreLabel));
        mExploreButton.setVisibility(visibility);
    }

    private void setResendEmail(int visibility) {
        mResendButton.setText(I18n.tr("Resend Email"));
        mResendButton.setVisibility(visibility);
    }

    private void setGiveupSignup(int visibility) {
        mResendButton.setText(I18n.tr("Return to Login"));
        mResendButton.setVisibility(visibility);
    }

    private void setProgressBar(int visibility) {
        mVerifyingProgress.setVisibility(visibility);
    }

    @Override
    public void onSetEmail() {
    }
}
