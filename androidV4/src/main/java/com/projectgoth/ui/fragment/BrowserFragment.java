/**
 * Copyright (c) 2013 Project Goth
 * BrowserFragment.java
 *
 * Jun 18, 2013 10:28:38 AM
 */

package com.projectgoth.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.common.migcommand.MigCommandsHandler;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.activity.LoginActivity;
import com.projectgoth.ui.listener.OnLoadWebListener;
import com.projectgoth.ui.widget.allaccessbutton.PageData;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.FiksuInterface;
import com.projectgoth.util.FiksuInterface.RecurringEvent;

/**
 * Fragment used to encapsulate the web view and render pagelets.
 *
 * @author angelorohit
 */
public class BrowserFragment extends BaseDialogFragment implements OnLoadWebListener {

    private final PageData                     mPageData              = createPageData();

    private static final String                LOG_TAG               = AndroidLogger.makeLogTag(BrowserFragment.class);
    public static final String                 PARAM_LAUNCH_URL      = "PARAM_LAUNCH_URL";
    public static final String                 PARAM_IS_FULL_URL     = "PARAM_IS_FULL_URL";
    private static final String                DISCOVER_PAGE_URL     = "https://discover.migxchat.net";
    private static final String                REGISTER_PAGE_URL     = "register.migxchat.net/register";
    private static final String                JAVASCRIPT_LOGIN      = "mig33:login()";

    private WebView                            mWebView               = null;
    private String                             mLaunchUrl             = Constants.BLANKSTR;
    private Boolean                            mIsFullyConstructedUrl = false;

    private WebViewClientExt                   mWebViewClientExt      = null;
    private View                               mMainView              = null;
    private VideoView                          mVideoView             = null;
    private FrameLayout                        mCustomViewLayout      = null;
    private WebChromeClient.CustomViewCallback mCustomViewCallback    = null;

    private String                             mDialogTitle           = null;
    private Drawable                           mDialogTitleIcon       = null;

    private String                             mPageTitle             = null;
    private int                                mPageTitleIcon         = R.drawable.ad_explore_white;

    // Used in overrideUrlLoading
    private static final String                SCHEME_WTAI           = "wtai://wp/";
    private static final String                SCHEME_WTAI_MC        = "wtai://wp/mc;";
    private static final String                SCHEME_WTAI_SD        = "wtai://wp/sd;";
    private static final String                SCHEME_WTAI_AP        = "wtai://wp/ap;";

    private boolean                            mIsDiscoverPage        = false;
    private TextView                           mTxtTitle;

    @Override
    public void onLoadWeb() {
        if (mLaunchUrl.equals(DISCOVER_PAGE_URL) || mLaunchUrl.equals(WebURL.URL_FORGOT_PASSWORD)) {
            goToPage(mLaunchUrl);
        }
    }

    /**
     * WebViewClient to override link clicks
     *
     * @author angelorohit
     */
    private class WebViewClientExt extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Logger.debug.log(LOG_TAG, "onPageStarted: " + url);

            // Fiksu hack!
            // Since we can only intercept the urls that the pagelet requests,
            // we have to process
            // the href of the JOIN button.
            if (url.endsWith(WebURL.URL_INVITE_VIA_EMAIL_SUBMIT)) {
                FiksuInterface.sendEvent(RecurringEvent.Referrals, ApplicationEx.getInstance().getCurrentActivity());
            } else if (url.contains("register/process")) {
                FiksuInterface.sendEvent(RecurringEvent.Registration_JoinButton, ApplicationEx.getInstance().getCurrentActivity());
            }
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return BrowserFragment.this.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            final String title = view.getTitle();
            if (!TextUtils.isEmpty(title) && TextUtils.isEmpty(mPageTitle)) {
                setTitle(title);
            }
            hideLoadProgressDialog();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Logger.error.log(LOG_TAG, "onReceivedSslError");
            hideLoadProgressDialog();
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.error.log(LOG_TAG, "onReceivedError with errorCode = ", errorCode,
                    " ,description = ", description, " ,url = ", failingUrl);
            hideLoadProgressDialog();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    public void setDialogTitle(final String title, final Drawable titleIcon) {
        this.mDialogTitle = title;
        this.mDialogTitleIcon = titleIcon;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.fragment.BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);

        if (bundleArgs != null) {
            mLaunchUrl = bundleArgs.getString(PARAM_LAUNCH_URL);
            mIsFullyConstructedUrl = bundleArgs.getBoolean(PARAM_IS_FULL_URL);
        }

        if (!TextUtils.isEmpty(mLaunchUrl) && mIsFullyConstructedUrl != null && mIsFullyConstructedUrl == false) {
            mLaunchUrl = Tools.constructProperUrl(mLaunchUrl);
        }

        ConnectionDetail detail = Config.getInstance().getConnectionDetail();
        if (mLaunchUrl != null && mLaunchUrl.equals(detail.getDiscoverServer())) {
            mIsDiscoverPage = true;
        }
    }

    /**
     * @see android.support.v4.app.Fragment#onCreateView(LayoutInflater,
     * ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // We use a different layout for the BrowserFragment depending on
        // whether it is being shown
        // as a dialog or not.
        final int layoutResId = FragmentHandler.getInstance().isFragmentShownAsDialog(this) ? R.layout.fragment_browser_dialog
                : R.layout.fragment_browser;
        mMainView = inflater.inflate(layoutResId, container, false);
        Activity activity = getActivity();
        if (activity instanceof LoginActivity) {
            ((LoginActivity) activity).setOnLoadWebListener(this);
        }
        // If a custom view has been shown, then use that customview.
        return (mCustomViewLayout == null) ? mMainView : mCustomViewLayout;
    }

    // Dummy
    protected int getLayoutId() {
        return -1;
    }

    /**
     * Overrides the loading of urls and decides what action to take based on
     * the url.
     *
     * @param view The WebView into which the url is being loaded.
     * @param url  The url being loaded.
     * @return true if the BrowserFragment takes over handling of the url and
     * false to let the host application handle it.
     */
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if (url.startsWith(SCHEME_WTAI)) {
            if (url.startsWith(SCHEME_WTAI_MC)) {
                // wtai://wp/mc;number
                // number=string(phone-number)
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_TEL
                        + url.substring(SCHEME_WTAI_MC.length())));
                startActivity(intent);
                return true;
            } else if (url.startsWith(SCHEME_WTAI_SD)) {
                // wtai://wp/sd;dtmf
                // dtmf=string(dialstring)
                return false;
            } else if (url.startsWith(SCHEME_WTAI_AP)) {
                // wtai://wp/ap;number;name
                // number=string(phone-number)
                // name=string
                return false;
            }
        }

        Activity activity = getActivity();

        if (url.contentEquals(JAVASCRIPT_LOGIN)) {
            //if BrowserFragment is in the LoginActivity, just change fragment
            if (activity instanceof LoginActivity) {
                LoginActivity loginActivity = (LoginActivity) activity;
                loginActivity.showPreloadedFragment(LoginActivity.PreloadedFragmentKey.LOGIN, true);
            } else {
                FragmentHandler.getInstance().showLoginActivity(activity);
            }

            return true;
        }

        if (url.startsWith(Constants.LINK_MIG33)) {
            processURL(mWebView, url);
            return true;
        }

        if (url.contains(REGISTER_PAGE_URL)) {
            if (activity instanceof LoginActivity) {
                LoginActivity loginActivity = (LoginActivity) activity;
                loginActivity.showPreloadedFragment(LoginActivity.PreloadedFragmentKey.USERNAME);
            } else {
                FragmentHandler.getInstance().showLoginActivity(activity, false, null, LoginActivity.PreloadedFragmentKey.USERNAME);
            }

            return true;
        }


        final boolean didStartActivity = UrlHandler.startActivityForUrl(getActivity(), url);
        if (didStartActivity) {
            // Launch url only if the build version is less than ICS.
            // Otherwise, let the browser manage loading by itself.
            if (!UIUtils.hasICS() && url.startsWith(Constants.LINK_HTTP) || url.startsWith(Constants.LINK_HTTPS)) {
                goToPage(url);
            } else {
                // Need to close this fragment before going to the
                // new activity to avoid coming back to a black screen.
                // e.g. Open a YouTube video
                closeFragment();
            }

            return true;
        }

        processURL(mWebView, url);
        return true;
    }

    public void processURL(WebView view, String url) {
        if (url.startsWith(Constants.LINK_MIG33)) {
            MigCommandsHandler.getInstance().handleCommandForUrl(url);
        } else {
            goToPage(url);
        }
    }

    /**
     * @see android.support.v4.app.Fragment#onViewCreated(View, Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initBrowserView(mMainView);

        mTxtTitle = (TextView) view.findViewById(R.id.txt_title);

        if (!TextUtils.isEmpty(mDialogTitle)) {
            if (mTxtTitle != null) {
                mTxtTitle.setText(mDialogTitle);

                if (mDialogTitleIcon != null) {
                    mTxtTitle.setCompoundDrawablesWithIntrinsicBounds(mDialogTitleIcon, null, null, null);
                }
            }
        }

        final ImageView btnClose = (ImageView) view.findViewById(R.id.btn_close);
        if (btnClose != null) {
            ImageHandler.tintDrawable(btnClose.getDrawable(), R.color.tint_lightgray);
            btnClose.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    closeFragment();
                }
            });
        }

        if (!mLaunchUrl.equals(DISCOVER_PAGE_URL) || !mLaunchUrl.equals(WebURL.URL_FORGOT_PASSWORD)) {
            goToPage(mLaunchUrl);
        }
        invokeOnViewCreated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadProgressDialog();
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }

    /**
     * Launches a specific url in the browser fragment
     *
     * @param url The url to be launched.
     */
    private void goToPage(final String url) {
        if (url != null && url.length() > 0 && mWebView != null) {

            if (mProgressImage != null) {
                mProgressImage.bringToFront();
                //exclude show progress dialog in preload discovery page
                if (!url.equals(DISCOVER_PAGE_URL)) {
                    showLoadProgressDialog();
                }

            }

            mWebView.loadUrl(url);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private final void initBrowserView(View view) {

        mWebView = (WebView) view.findViewById(R.id.webview);

        if (mWebView != null) {
            // Set up web view
            mWebView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onShowCustomView(View view, CustomViewCallback callback) {
                    super.onShowCustomView(view, callback);
                    if (view instanceof FrameLayout) {
                        mCustomViewLayout = (FrameLayout) view;
                        mCustomViewCallback = callback;

                        View focusedChild = mCustomViewLayout.getFocusedChild();
                        if (focusedChild instanceof VideoView) {
                            mVideoView = (VideoView) focusedChild;
                            mMainView.setVisibility(View.GONE);
                            mCustomViewLayout.setVisibility(View.VISIBLE);
                            mVideoView.setOnCompletionListener(new OnCompletionListener() {

                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mp.stop();
                                    mCustomViewLayout.setVisibility(View.GONE);
                                    onHideCustomView();
                                    closeVideoView();
                                }
                            });
                            mVideoView.setOnErrorListener(new OnErrorListener() {

                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    closeVideoView();
                                    return true;
                                }
                            });
                            mVideoView.start();
                        }
                    }
                }
            });

            mWebViewClientExt = new WebViewClientExt();
            mWebView.setWebViewClient(mWebViewClientExt);
            mWebView.setVerticalScrollbarOverlay(true);
            mWebView.requestFocus(View.FOCUS_DOWN);
            mWebView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                            break;
                    }

                    return false;
                }
            });

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            UIUtils.setPluginState(webSettings, true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setDomStorageEnabled(true);
            webSettings.setSaveFormData(false);
            webSettings.setSavePassword(false);
            webSettings.setUserAgentString(Tools.getUserAgentForBrowser(mWebView));
            webSettings.setLoadsImagesAutomatically(true);
            webSettings.setBlockNetworkImage(false);

            mWebView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // Override back key press to navigate to the previous page.
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (closeVideoView()) {
                            return true;
                        }
                        if (mWebView != null && mWebView.canGoBack()) {
                            mWebView.goBack();
                            return true;
                        }
                    }

                    return false;
                }
            });
        }
    }

    /**
     * @see android.support.v4.app.Fragment#onSaveInstanceState(Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mWebView != null) {
            mWebView.saveState(outState);
        }
    }

    /**
     * Closes the video view (if one was present) and removes the custom view in
     * the fragment.
     *
     * @return true if the custom view was removed and false otherwise.
     */
    private boolean closeVideoView() {
        if (mCustomViewLayout != null) {
            mCustomViewLayout.setVisibility(View.GONE);

            if (mVideoView != null) {
                mVideoView.stopPlayback();
                mVideoView.setVisibility(View.GONE);
                mCustomViewLayout.removeView(mVideoView);
                mVideoView = null;
                mCustomViewLayout.setVisibility(View.GONE);
                mCustomViewCallback.onCustomViewHidden();

                mMainView.setVisibility(View.VISIBLE);
            }

            mCustomViewLayout = null;

            return true;
        }

        return false;
    }

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#registerReceivers()
     */
    @Override
    protected void registerReceivers() {
        registerEvent(Events.Login.SUCCESS);
        registerEvent(AppEvents.Application.SHOW_SOCIAL_SPACE);
        registerEvent(Events.User.UPLOADED_TO_PHOTO_ALBUM);
    }

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.Login.SUCCESS)) {
            goToPage(mLaunchUrl);
        } else if (action.equals(AppEvents.Application.SHOW_SOCIAL_SPACE)) {
            goToPage(mLaunchUrl);
        } else if (action.equals(Events.User.UPLOADED_TO_PHOTO_ALBUM)) {
            mWebView.reload();
        }
    }

    @Override
    protected int getTitleIcon() {
        return mPageTitleIcon;
    }

    @Override
    protected String getTitle() {
        if (mIsDiscoverPage) {
            return I18n.tr("Explore");
        }

        return (mPageTitle != null) ? mPageTitle : I18n.tr("mig");
    }

    @Override
    public void updateTitle() {
        if (!getCurrentTitle().matches(getTitle())) {
            super.updateTitle();
            if (mIsDiscoverPage) {
                showTitleAnimation();
            }
        }
    }

    @Override
    public void updateIcon() {
        if (getCurrentTitleIconTag() == 0 || getCurrentTitleIconTag() != getTitleIcon()) {
            super.updateIcon();
            if (mIsDiscoverPage) {
                showTitleIconAnimation();
            }
        }
    }

    public void setBrowserTitle(final String title, final int titleIcon) {
        this.mPageTitle = title;
        this.mPageTitleIcon = titleIcon;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.HANDBURGUER);
        config.setShowOverflowButtonState(OverflowButtonState.ALERT);
        return config;
    }

    @Override
    public void onAttach(Activity activity) {
        if (mIsDiscoverPage) {
            setShouldUpdateActionBarOnAttach(false);
        }
        super.onAttach(activity);
    }

    @Override
    public PageData getPageData() {
        return mPageData;
    }

    private final PageData createPageData() {
        return new PageData(R.drawable.ad_explore_orange);
    }

    public void manualResumeTimer() {
        if (mWebView != null) {
            mWebView.resumeTimers();
        }
    }

    public void manualPauseTimer(boolean toPauseFirstTime) {
        ApplicationEx.getInstance().setFirstTimeRunApp(toPauseFirstTime);
        if (mWebView != null) {
            mWebView.pauseTimers();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWebView != null && !ApplicationEx.isActivityVisible()) {
            mWebView.pauseTimers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            if (ApplicationEx.getInstance().isFirstTimeRunApp()) {
                ApplicationEx.getInstance().setFirstTimeRunApp(false);
                mWebView.pauseTimers();
            } else {
                mWebView.resumeTimers();
            }
        }
    }
}