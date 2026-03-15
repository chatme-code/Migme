package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.common.Version;
import com.projectgoth.events.GAEvent;
import com.projectgoth.ui.activity.LoginActivity;


/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupFacebookFailFragment extends BaseSignupVerifyFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.FACEBOOK_FAIL.toString();
        setPageLayout(VerifiedResultType.FACEBOOK_FAIL);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_email_verify_explore_button:
                ((LoginActivity) getActivity()).showPreloadedFragment(LoginActivity.PreloadedFragmentKey.USERNAME);
                break;
        }
    }
}
