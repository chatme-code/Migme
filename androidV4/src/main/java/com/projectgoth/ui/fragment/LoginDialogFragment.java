package com.projectgoth.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.activity.LoginActivity;

/**
 * Created by shiyukun on 12/5/15.
 */
public class LoginDialogFragment extends BaseDialogFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.login_dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = (TextView)view.findViewById(R.id.login_dialog_title);
        TextView login = (TextView)view.findViewById(R.id.login_dialog_login);
        TextView register = (TextView)view.findViewById(R.id.login_dialog_register);
        title.setText(I18n.tr("Log in or sign up now to interact"));
        login.setText(I18n.tr("LOG IN"));
        register.setText(I18n.tr("SIGN UP"));
        View.OnClickListener clickListener = new View.OnClickListener(){
            public void onClick(View view){
                closeFragment();
                FragmentHandler.getInstance().showLoginActivity(getActivity());
            }
        };
        login.setOnClickListener(clickListener);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
                FragmentHandler.getInstance().showLoginActivity(getActivity(), true, null, LoginActivity.PreloadedFragmentKey.USERNAME);
            }
        });
    }
}
