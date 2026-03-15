package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.nemesis.listeners.CleanAllFieldsListener;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.listener.SetBannerListener;
import com.projectgoth.ui.listener.SetShowLoadingListener;

/**
 * Created by justinhsu on 4/10/15.
 */
public abstract class BaseSignupFragment extends BaseFragment implements View.OnClickListener, View.OnKeyListener, SetBannerListener, SetShowLoadingListener, CleanAllFieldsListener {

    protected String mCurrentFragmentKey = "";

    @Override
    protected int getLayoutId() {
        return 0;
    }

    public enum BannerType {
        NORMAL, UNAVAILABLE, AVAILABLE, ERROR, SUCCESS, RETURN_ERROR
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginActivity activity = (LoginActivity) getActivity();
        activity.setErrorBannerListener(mCurrentFragmentKey, this);
        activity.setShowLoadingListener(mCurrentFragmentKey, this);
        activity.setCleanFieldListener(mCurrentFragmentKey, this);

        toShowLoading(false);
        setBanner(BannerType.NORMAL);
    }

    protected abstract void setBanner(BannerType type);

    protected abstract void setBannerResource(String slognText, int slognLayout, int slognView, String hint, int hintColor);

    protected String getInput() {
        return "";
    }

    protected void toNextPage(String dataToStore) {
        final LoginActivity activity = ((LoginActivity) getActivity());
        if (activity != null) {
            activity.toNextPage(dataToStore);
        }
    }

    protected void validateInputWithServer(String dataToStore) {
        toShowLoading(true);
    }

    protected abstract boolean validateInputWithRegularExpress(String dataToStore);

    protected void validateInput(String input) {
        /** to check is matched regular expression or not */
        if (validateInputWithRegularExpress(input)) {
            validateInputWithServer(input);
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            validateInput(getInput());
            return true;
        }
        return false;
    }

    @Override
    public void onFieldClean() {

    }

    @Override
    public void onSetBanner(BannerType type) {
        setBanner(type);
    }

    @Override
    public void onSetLoading(boolean isShow) {
        toShowLoading(isShow);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.nextButton:
                validateInput(getInput());
                break;
            case R.id.previousButton:
                final LoginActivity activity = ((LoginActivity) getActivity());
                if (activity != null) {
                    activity.onBackPressed();
                }
                break;
        }
    }

    protected abstract void toShowLoading(boolean isShow);
}
