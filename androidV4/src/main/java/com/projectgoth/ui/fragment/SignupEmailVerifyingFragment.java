package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.LoginActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by justinhsu on 4/8/15.
 */
public class SignupEmailVerifyingFragment extends BaseSignupVerifyFragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentFragmentKey = LoginActivity.PreloadedFragmentKey.VERIFTYING_TOKEN.toString();
        setPageLayout(VerifiedResultType.VERIFYING);
    }
}
