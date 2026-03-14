package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.projectgoth.R;
import com.projectgoth.common.Version;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.listeners.GetSuggestUsersListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.widget.DateTextView;
import com.projectgoth.ui.widget.SuggestedNameBox;

import java.util.List;

/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupUsernameFragment extends BaseSignupFragment {
    private EditText                mUsername;
    private TextView                mSloganText;
    private ImageView               mSloganView;
    private ImageView               mSloganViewIndicator;
    private RelativeLayout          mSloganlayout;
    private Button                  mFacebookSignup;
    private TextView                mTermOfUse;
    private TextView                mTermOfUsePhrase;
    private TextView                mSuggestedTitle;
    private TextView                mPrivacyPolicy;
    private TextView                mPrivacyPolicyPhrase;
    private Button                  mPeekButton;
    private Button                  mSignupButton;
    private DateTextView            mOrLineText;
    private Button                  mLoginButton;
    private ImageButton             mNextButton;
    private TextView                mUsernameHint;
    private RelativeLayout          mSuggestedNameBody;
    private LinearLayout            mSignupInputContainer;
    private SuggestedNameBox        mSuggestedNameBox;
    private ProgressBar             mNextStepProgress;
    private GetSuggestUsersListener mGetSuggestUsersListener;
    private String                  mUsernameRegularExp                     = "^[a-zA-Z]{1}(\\w{5,20})$";
    private static int              SHOW_SUGGESTION_NAME_ANIMATION_DURATION = 2500;
    private static int              HIDE_SUGGESTION_NAME_ANIMATION_DURATION = 500;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup_create_name;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LoginActivity activity = (LoginActivity) getActivity();
        mUsername = (EditText) view.findViewById(R.id.signup_username);
        mUsername.requestFocus();
        mUsernameHint = (TextView) view.findViewById(R.id.username_hint);
        mSloganlayout = (RelativeLayout) view.findViewById(R.id.slogan_container);
        mSloganText = (TextView) view.findViewById(R.id.pick_name_slogan);
        mSloganView = (ImageView) view.findViewById(R.id.pick_name_image);
        mNextStepProgress = (ProgressBar) view.findViewById(R.id.progressbar);
        mNextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mFacebookSignup = (Button) view.findViewById(R.id.btn_fbconnect);
        mTermOfUse = (TextView) view.findViewById(R.id.txt_terms_of_use_link);
        mTermOfUse.setOnClickListener(activity);
        mTermOfUse.setText(I18n.tr("terms of use."));
        mSloganViewIndicator = (ImageView) view.findViewById(R.id.img_fragment_indicator);
        mTermOfUsePhrase = (TextView) view.findViewById(R.id.txt_terms_of_use);
        mTermOfUsePhrase.setText(I18n.tr("By signing up, you agree to our "));
        mPrivacyPolicyPhrase = (TextView) view.findViewById(R.id.txt_privacy_policy);
        mPrivacyPolicyPhrase.setText(I18n.tr("View "));
        mPrivacyPolicy = (TextView) view.findViewById(R.id.txt_privacy_policy_link);
        mPrivacyPolicy.setText(I18n.tr("privacy policy."));
        mPeekButton = (Button) view.findViewById(R.id.btn_discover);
        mLoginButton = (Button) view.findViewById(R.id.btn_login);
        mSignupButton = (Button) view.findViewById(R.id.signupButton);
        mSuggestedNameBody = (RelativeLayout) view.findViewById(R.id.signup_suggestion_container);
        mSuggestedNameBody.setVisibility(View.GONE);
        mSuggestedNameBox = (SuggestedNameBox) view.findViewById(R.id.signup_suggested_linear);
        mSignupInputContainer = (LinearLayout) view.findViewById(R.id.signup_input_container);
        mSuggestedTitle = (TextView) view.findViewById(R.id.signup_suggestion_title);
        mOrLineText = (DateTextView) view.findViewById(R.id.or_line_text);
        mOrLineText.setText(I18n.tr("Or"));
        mSignupButton.setText(I18n.tr("Sign up"));
        mLoginButton.setText(I18n.tr("Log in"));
        mSuggestedTitle.setText(I18n.tr("Some suggested names"));
        mUsername.setOnKeyListener(this);
        mUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mUsername.setHint(I18n.tr("Username"));
        mNextButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(activity);
        mFacebookSignup.setOnClickListener(activity);
        mPrivacyPolicy.setOnClickListener(activity);
        mPeekButton.setOnClickListener(activity);
        mPeekButton.setText(I18n.tr("PEEK INSIDE"));
        mGetSuggestUsersListener = new GetSuggestUsersListener() {
            @Override
            public void onGetSuggestUsers(final List<String> userList) {
                final LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            long interval = activity.calculateResponsePeriod();
                            if (interval > 0) {
                                GAEvent.Signup_Timing_Validate_Username.sendTiming(Version.getVasTrackingId(), interval);
                            }
                            toShowLoading(false);
                            if (!userList.isEmpty()) {
                                if (activity.isInPopbackForNewInputCondition()) {
                                    GAEvent.Signup_ErrorHandle_UsernameErrorType.send(Version.getVasTrackingId());
                                } else if (activity.getCurrentSignupType() == LoginActivity.SignupType.FACEBOOK) {
                                    GAEvent.Signup_Facebook_UsernameFailure.send(Version.getVasTrackingId());
                                } else {
                                    GAEvent.SignUp_UsernameFailure.send(Version.getVasTrackingId());
                                }
                                setBanner(BannerType.ERROR);
                                setSuggestedName(userList);
                            } else {
                                setBanner(BannerType.SUCCESS);
                                if (activity.isInPopbackForNewInputCondition()) {
                                    GAEvent.Signup_ErrorHandle_UsernameProceed.send(Version.getVasTrackingId());
                                    GAEvent.Signup_ErrorHandle_UsernameSuccess.send(Version.getVasTrackingId());
                                } else if (activity.getCurrentSignupType() == LoginActivity.SignupType.FACEBOOK) {
                                    GAEvent.Signup_Facebook_UsernameSuccess.send(Version.getVasTrackingId());
                                } else {
                                    GAEvent.SignUp_UsernameSuccess.send(Version.getVasTrackingId());
                                }
                                toNextPage(getInput());
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
                                GAEvent.Signup_Timing_Validate_Username.sendTiming(Version.getVasTrackingId(), interval);
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
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.USERNAME.toString();

        addButtonToNoConnectionDisableButtonList(mNextButton);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected boolean validateInputWithRegularExpress(String input) {
        if (input.matches(mUsernameRegularExp)) {
            return true;
        } else {
            setBanner(BannerType.NORMAL);
        }
        return false;
    }

    protected void setSuggestedName(final List<String> suggestedNames) {
        mSuggestedNameBox.removeAllViews();
        for (String suggestedName : suggestedNames) {
            TextView tv = createTextView(suggestedName);
            if (tv != null) {
                mSuggestedNameBox.addItem(tv);
            }
        }
        suggestedNameVisibility(true, false);
    }

    private TextView createTextView(final String name) {
        TextView item = null;
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            item = new TextView(activity);
            item.setPadding(getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_padding), getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_margin), getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_padding), getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_margin));
            item.setMaxLines(1);
            item.setBackgroundResource(R.drawable.signup_suggested_name_bg);
            item.setTextColor(getResources().getColor(R.color.white_text_color));
            item.setText(name);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_padding), 0, getResources().getDimensionPixelSize(R.dimen.signup_suggested_item_padding), 0);
            item.setLayoutParams(params);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUsername.setText(name);
                    suggestedNameVisibility(false, false);
                    setBanner(BannerType.SUCCESS);
                }
            });
        }
        return item;
    }

    @Override
    public void setBannerResource(final String slognText, final int slognLayout, final int slognView, final String hint, final int hintColor) {
        mSloganText.setText(slognText);
        mSloganlayout.setBackgroundColor(getResources().getColor(slognLayout));
        mSloganView.setImageResource(slognView);
        mUsernameHint.setText(hint);
        mUsernameHint.setTextColor(getResources().getColor(hintColor));
    }

    @Override
    public void setBanner(BannerType type) {
        setIndicator(type);
        switch (type) {
            case NORMAL:
                setBannerResource(I18n.tr("Pick something nice"), R.color.signup_normal, R.drawable.ad_greenbot_bigsmile, I18n.tr("Min. 6 characters, starting with a letter"), R.color.signup_text_light_grey);
                break;
            case UNAVAILABLE:
                setBannerResource(I18n.tr("Oops, this name is unavailable"), R.color.signup_normal, R.drawable.ad_yellowbot_sweat, I18n.tr("Min. 6 characters, starting with a letter"), R.color.signup_text_light_grey);
                break;
            case ERROR:
                setBannerResource(I18n.tr("C'mon, you can do better than this!"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("Username should be 6 - 20 characters long, and start with a letter"), R.color.signup_text_red);
                break;
            case SUCCESS:
                setBannerResource(I18n.tr("That looks alright"), R.color.signup_normal, R.drawable.ad_yellowbot_smirk, I18n.tr("Min. 6 characters, starting with a letter"), R.color.signup_text_light_grey);
                break;
            case RETURN_ERROR:
                setBannerResource(I18n.tr("Oops, this name is unavailable"), R.color.signup_error, R.drawable.ad_yellowbot_sweat, I18n.tr("Please enter another username"), R.color.signup_text_red);
                break;
        }
    }

    protected void suggestedNameVisibility(final boolean isVisible, boolean forceHide) {
        if (forceHide) {
            mSuggestedNameBody.setVisibility(View.GONE);
            mSuggestedNameBody.clearAnimation();
        } else if (isVisible) {
            if (mSuggestedNameBody.getVisibility() == View.GONE) {
                final LoginActivity activity = ((LoginActivity) getActivity());
                if (activity != null) {
                    Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
                    animation.setDuration(SHOW_SUGGESTION_NAME_ANIMATION_DURATION);
                    mSuggestedNameBody.startAnimation(animation);
                    mSignupInputContainer.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            mSuggestedNameBody.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        } else {
            if (mSuggestedNameBody.getVisibility() == View.VISIBLE) {
                final LoginActivity activity = ((LoginActivity) getActivity());
                if (activity != null) {
                    Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out);
                    animation.setDuration(HIDE_SUGGESTION_NAME_ANIMATION_DURATION);
                    mSuggestedNameBody.startAnimation(animation);
                    mSignupInputContainer.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mSuggestedNameBody.setVisibility(View.GONE);

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        }
    }

    public void setIndicator(BannerType type) {
        if (type == BannerType.ERROR) {
            mSloganViewIndicator.setImageResource(R.drawable.ad_triangle_red);
        } else {
            mSloganViewIndicator.setImageResource(R.drawable.ad_triangle_green);
        }
    }

    @Override
    protected void validateInputWithServer(String input) {
        super.validateInputWithServer(input);
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.checkUserName(input, mGetSuggestUsersListener);
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

    public void setUserName(String userName) {
        if (!TextUtils.isEmpty(mUsername.getText())) {
            mUsername.setText(userName);
        }
    }

    public String getInput() {
        return mUsername.getText().toString();
    }

    @Override
    public void onFieldClean() {
        suggestedNameVisibility(false, true);
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null && mUsername != null) {
            mUsername.setText(activity.getSignupData(LoginActivity.PreloadedFragmentKey.USERNAME));
        }
    }

    @Override
    public void onSetBanner(BannerType type) {
        suggestedNameVisibility(false, true);
        super.onSetBanner(type);
    }
}
