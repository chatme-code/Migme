package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.ui.activity.LoginActivity;


/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupEmailResultExpiredFragment extends BaseSignupVerifyFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.TOKEN_EXPIRED.toString();
        setPageLayout(VerifiedResultType.TOKEN_EXPIRED);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.signup_email_verify_explore_button:
                final LoginActivity activity = ((LoginActivity) getActivity());
                if (activity != null) {
                    activity.showPreloadedFragment(LoginActivity.PreloadedFragmentKey.USERNAME);
                }
                break;
        }
    }
}

