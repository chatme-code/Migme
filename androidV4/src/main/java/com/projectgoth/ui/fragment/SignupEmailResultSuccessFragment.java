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
public class SignupEmailResultSuccessFragment extends BaseSignupVerifyFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.TOKEN_SUCCESS.toString();
        setPageLayout(VerifiedResultType.TOKEN_SUCCESS);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_email_verify_explore_button:
                GAEvent.Signup_Verify_Success.send(Version.getVasTrackingId());
                final LoginActivity activity = ((LoginActivity) getActivity());
                if (activity != null) {
                    activity.showPreloadedFragment(LoginActivity.PreloadedFragmentKey.LOGIN);
                }
                break;
        }
    }
}
