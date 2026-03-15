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
import com.projectgoth.common.Version;
import com.projectgoth.controller.FacebookLoginController;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.listeners.CheckEmailValidationListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.LoginActivity;


/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupEmailFragment extends BaseSignupFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup_create_email;
    }

    private EditText                        mEmail;
    private TextView                        mSlognText;
    private TextView                        mTitleText;
    private ImageView                       mSlognView;
    private RelativeLayout                  mSlognlayout;
    private ImageButton                     mNextButton;
    private ImageButton                     mPreviousButton;
    private TextView                        mEmailHint;
    private ProgressBar                     mNextStepProgress;
    private CheckEmailValidationListener    mCheckEmailValidationListener;
    private String                          mEmailRegularExp                = "^[A-Za-z0-9_.]+@[a-zA-Z_]+?\\.[a-zA-Z]+";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEmail = (EditText) view.findViewById(R.id.signup_email);
        mSlognlayout = (RelativeLayout) view.findViewById(R.id.slogan_container);
        mSlognText = (TextView) view.findViewById(R.id.pick_email_slogan);
        mTitleText = (TextView) view.findViewById(R.id.title);
        mSlognView = (ImageView) view.findViewById(R.id.pick_email_image);
        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) view.findViewById(R.id.previousButton);
        mPreviousButton.setOnClickListener(this);
        mNextStepProgress = (ProgressBar) view.findViewById(R.id.progressbar);
        mEmailHint = (TextView) view.findViewById(R.id.email_hint);
        mTitleText.setText(I18n.tr("Enter your email"));
        mEmail.setOnKeyListener(this);
        mEmail.setHint(I18n.tr("Email"));
        mNextButton.setOnClickListener(this);
        mCheckEmailValidationListener = new CheckEmailValidationListener() {
            @Override
            public void onCheckEmailValidation(final boolean result) {
                final LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            long interval = activity.calculateResponsePeriod();
                            if (interval > 0) {
                                GAEvent.Signup_Timing_Validate_Email.sendTiming(Version.getVasTrackingId(), interval);
                            }
                            toShowLoading(false);
                            if (result) {
                                setBanner(BannerType.SUCCESS);
                                toNextPage(getInput());
                                if (activity.isInPopbackForNewInputCondition()) {
                                    GAEvent.Signup_ErrorHandle_EmailSuccess.send(Version.getVasTrackingId());
                                    GAEvent.Signup_ErrorHandle_EmailProceed.send(Version.getVasTrackingId());
                                } else {
                                    GAEvent.SignUp_EmailSuccess.send(Version.getVasTrackingId());
                                }
                            } else {
                                toShowLoading(false);
                                setBanner(BannerType.ERROR);
                                if (activity.isInPopbackForNewInputCondition()) {
                                    GAEvent.Signup_ErrorHandle_EmailErrorType.send(Version.getVasTrackingId());
                                } else {
                                    GAEvent.SignUp_EmailFailure.send(Version.getVasTrackingId());
                                }
                            }
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
                                GAEvent.Signup_Timing_Validate_Email.sendTiming(Version.getVasTrackingId(), interval);
                            }
                            switch (errType) {
                                case HTTP_REQUEST_TIMEOUT:
                                    toShowLoading(false);
                                    GAEvent.Signup_API_TimeOut.send(Version.getVasTrackingId());
                                    Toast.makeText(activity, I18n.tr("Connection timeout, please try again later!"), Toast.LENGTH_SHORT).show();
                                    break;
                                case HTTP_WORKER_ERROR:
                                    toShowLoading(false);
                                    Toast.makeText(activity, I18n.tr("Cannot connect to server."), Toast.LENGTH_SHORT).show();
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
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.EMAIL.toString();
        String facebookEmail = FacebookLoginController.getInstance().getFacebookEmail();
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            if (activity.getCurrentSignupType() == LoginActivity.SignupType.FACEBOOK && !TextUtils.isEmpty(facebookEmail)) {
                setEmail(facebookEmail);
            }
            activity.addOnFragmentShowListener(LoginActivity.PreloadedFragmentKey.EMAIL, new LoginActivity.OnFragmentShowListener() {
                @Override
                public void show() {
                    mEmail.requestFocus();
                }
            });
        }

        addButtonToNoConnectionDisableButtonList(mNextButton);
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void setBannerResource(final String slognText, final int slognLayout, final int slognView, final String hint, final int hintColor) {
        mSlognText.setText(slognText);
        mSlognlayout.setBackgroundColor(getResources().getColor(slognLayout));
        mSlognView.setImageResource(slognView);
        mEmailHint.setText(hint);
        mEmailHint.setTextColor(getResources().getColor(hintColor));
    }

    @Override
    public void setBanner(BannerType type) {
        switch (type) {
            case NORMAL:
                setBannerResource(I18n.tr("In case we need to contact you"), R.color.signup_normal, R.drawable.ad_purplebot_email, I18n.tr("A verification email will be sent to this address"), R.color.signup_text_light_grey);
                break;
            case ERROR:
                setBannerResource(I18n.tr("C'mon, you can do better than this!"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("This email has been used!"), R.color.signup_text_red);
                break;
            case SUCCESS:
                setBannerResource(I18n.tr("In case we need to contact you"), R.color.signup_normal, R.drawable.ad_purplebot_email, I18n.tr("A verification email will be sent to this address"), R.color.signup_text_light_grey);
                break;
            case RETURN_ERROR:
                setBannerResource(I18n.tr("This email address is not valid"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("Please enter a valid email address"), R.color.signup_text_red);
                break;
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

    public void setEmail(String email) {
        if (!TextUtils.isEmpty(mEmail.getText())) {
            mEmail.setText(email);
        }
    }

    public String getInput() {
        return mEmail.getText().toString();
    }

    @Override
    protected boolean validateInputWithRegularExpress(String input) {
        if (input.matches(mEmailRegularExp)) {
            return true;
        } else {
            setBanner(BannerType.NORMAL);
            final LoginActivity activity = ((LoginActivity) getActivity());
            if (activity != null) {
                Toast.makeText(activity, I18n.tr("Please enter a valid email address!"), Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    protected void validateInputWithServer(String input) {
        super.validateInputWithServer(input);
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.checkEmail(input, mCheckEmailValidationListener);
        }
    }

    @Override
    public void onFieldClean() {
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null && mEmail != null) {
            mEmail.setText(activity.getSignupData(LoginActivity.PreloadedFragmentKey.EMAIL));
        }
    }
}
