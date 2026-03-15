package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.common.Version;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.LoginActivity;

/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupPasswordFragment extends BaseSignupFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup_create_password;
    }

    private EditText        mPassword;
    private TextView        mSlognText;
    private TextView        mTitleText;
    private ImageView       mSlognView;
    private RelativeLayout  mSlognlayout;
    private ImageButton     mNextButton;
    private ImageButton     mPreviousButton;
    private TextView        mShowHidePasswordButton;
    private TextView        mPasswordHint;
    private ProgressBar     mNextStepProgress;
    private String          mPasswordRegularExp         =  "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mPassword = (EditText) view.findViewById(R.id.signup_password);
        mPassword.requestFocus();
        mNextStepProgress = (ProgressBar) view.findViewById(R.id.progressbar);
        mPasswordHint = (TextView) view.findViewById(R.id.password_hint);
        mTitleText = (TextView) view.findViewById(R.id.title);
        mShowHidePasswordButton = (TextView) view.findViewById(R.id.showHidePassword);
        mSlognlayout = (RelativeLayout) view.findViewById(R.id.slogan_container);
        mSlognText = (TextView) view.findViewById(R.id.pick_password_slogan);
        mSlognView = (ImageView) view.findViewById(R.id.pick_password_image);
        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) view.findViewById(R.id.previousButton);
        mPreviousButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mShowHidePasswordButton.setOnClickListener(this);
        mPassword.setOnKeyListener(this);
        mPassword.setHint(I18n.tr("Password"));
        mTitleText.setText(I18n.tr("Choose Password"));
        showHidePasswordButton();
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.PASSWORD.toString();
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.addOnFragmentShowListener(LoginActivity.PreloadedFragmentKey.PASSWORD, new LoginActivity.OnFragmentShowListener() {
                @Override
                public void show() {
                    mPassword.requestFocus();
                }
            });
        }

        addButtonToNoConnectionDisableButtonList(mNextButton);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int viewId = view.getId();
        switch (viewId) {
            case R.id.showHidePassword:
                GAEvent.SignUp_PasswordHide.send(Version.getVasTrackingId());
                showHidePasswordButton();
                break;
        }
    }

    protected void showHidePasswordButton() {
        if ((mShowHidePasswordButton.getText().toString()).equals(I18n.tr("Show Password"))) {
            mShowHidePasswordButton.setText(I18n.tr("Hide Password"));
            mPassword.setTransformationMethod(null);
            mPassword.setSelection(mPassword.length());
        } else {
            mShowHidePasswordButton.setText(I18n.tr("Show Password"));
            mPassword.setTransformationMethod(new PasswordTransformationMethod());
            mPassword.setSelection(mPassword.length());
        }
    }

    @Override
    public void setBannerResource(final String slognText, final int slognLayout, final int slognView, final String hint, final int hintColor) {
        mSlognText.setText(slognText);
        mSlognlayout.setBackgroundColor(getResources().getColor(slognLayout));
        mSlognView.setImageResource(slognView);
        mPasswordHint.setText(hint);
        mPasswordHint.setTextColor(getResources().getColor(hintColor));
    }

    @Override
    public void setBanner(BannerType type) {
        switch (type) {
            case NORMAL:
                setBannerResource(I18n.tr("Keep your password safe!"), R.color.signup_normal, R.drawable.ad_yellowbot_fingerpoint, I18n.tr("Min. 6 characters, with letters and numbers"), R.color.signup_text_light_grey);
                break;
            case ERROR:
                setBannerResource(I18n.tr("C'mon, you can do better than this!"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("Min. 6 characters, with both numbers and letters, and no special characters"), R.color.signup_text_red);
                break;
            case AVAILABLE:
                setBannerResource(I18n.tr("That looks alright"), R.color.signup_normal, R.drawable.ad_yellowbot_smirk, I18n.tr("Min. 6 characters, with letters and numbers"), R.color.signup_text_light_grey);
                break;
            case SUCCESS:
                setBannerResource(I18n.tr("Love it!"), R.color.signup_normal, R.drawable.ad_yellowbot_love, I18n.tr("Min. 6 characters, with letters and numbers"), R.color.signup_text_light_grey);
                break;
            case RETURN_ERROR:
                setBannerResource(I18n.tr("C'mon, you can do better than this!"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("This password is too weak, please choose another password. Password may not contain special characters, sequential numbers, and ‘password’."), R.color.signup_text_red);
                break;
        }
    }


    public String getInput() {
        return mPassword.getText().toString();
    }

    @Override
    protected boolean validateInputWithRegularExpress(String input) {
        if (input.length() > 0) {
            if (input.matches(mPasswordRegularExp)) {
                if (input.length() >= 10) {
                    setBanner(BannerType.SUCCESS);
                } else if (input.length() >= 6) {
                    setBanner(BannerType.AVAILABLE);
                } else {
                    setBanner(BannerType.NORMAL);
                    return false;
                }
                return true;
            } else {
                setBanner(BannerType.ERROR);
            }
        } else {
            setBanner(BannerType.NORMAL);
        }
        return false;
    }

    @Override
    protected void validateInputWithServer(String input) {
        super.validateInputWithServer(input);
        toShowLoading(false);
        toNextPage(input);
    }

    public void toShowLoading(final boolean isShow) {
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
    public void onFieldClean() {
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null && mPassword != null) {
            mPassword.setText(activity.getSignupData(LoginActivity.PreloadedFragmentKey.PASSWORD));
        }
    }
}
