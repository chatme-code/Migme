package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.ui.activity.LoginActivity;


/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupEmailVerifyFragment extends BaseSignupVerifyFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.SUCCESS.toString();
        super.onViewCreated(view, savedInstanceState);
        setPageLayout(VerifiedResultType.SUCCESS);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.signup_email_verify_explore_button:
                ((LoginActivity) getActivity()).resendEmail();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showEmailAddress();
    }

    @Override
    public void onSetEmail() {
        showEmailAddress();
    }
}
