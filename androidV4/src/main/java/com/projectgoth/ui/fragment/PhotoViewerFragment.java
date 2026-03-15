/**
 * Copyright (c) 2013 Project Goth
 *
 * PhotoViewerFragment.java
 * Created Sep 13, 2013, 11:40:04 AM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.TouchImageView;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.AnimUtils;
import com.projectgoth.util.StorageUtils;
import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;
import com.projectgoth.util.scheduler.ScheduledJobsHandler;
import com.projectgoth.util.scheduler.ScheduledJobsHandler.ScheduledJobKeys;

/**
 * @author angelorohit
 * 
 */
public class PhotoViewerFragment extends BaseFragment implements OnClickListener, ContextMenuItemListener {

    private static final String LOG_TAG                    = AndroidLogger.makeLogTag(PhotoViewerFragment.class);

    // Parameter for the image url that will be displayed as the photo in this
    // fragment.
    public static final String  PARAM_IMAGE_URL            = "PARAM_IMAGE_URL";

    // Parameter for the username of the sender of the photo in this fragment.
    public static final String  PARAM_PHOTO_SENDER         = "PARAM_PHOTO_SENDER";

    // Parameter that can be used to show or hide save to device icon.
    public static final String  PARAM_ALLOW_SAVE_TO_DEVICE = "PARAM_ALLOW_SAVE_TO_DEVICE";

    // Parameter that is used to determine whether the url passed in PARAM_IMAGE_URL is meant to be 
    // displayed in the web view or not.
    public static final String  PARAM_IS_URL_FOR_WEB_PAGE  = "PARAM_IS_URL_FOR_WEB_PAGE";

    private TouchImageView      image                   = null;
    private WebView             webView                 = null;
    private ProgressBar         prgIndicator            = null;
    private ImageView           imgClose                = null;
    private ImageView           imgSave                 = null;
    private ImageView           imgShare                = null;
    private ImageView           imgOptions              = null;

    private RelativeLayout      topControlsContainer    = null;
    private LinearLayout        bottomControlsContainer = null;

    // Tapping on the photo toggles fading in or fading out of the controls on
    // the screen.
    private boolean             didControlsFadeOut      = false;

    // The image url that is the photo of in this fragment.
    private String              imageUrl                = null;
    // The username of the sender of this photo image.
    private String              photoSender             = null;
    // Whether save to device should be enabled or not.
    private boolean             shouldAllowSaveToDevice = true;
    // Whether the imageUrl is meant to be displayed in a web view or not.
    private boolean             isUrlForWebPage         = false;
    
    // If set, this bitmap will be used as the image to be shown instead of loading from imageUrl. 
    private Bitmap              imageBitmap             = null;

    public void setImageBitmap(final Bitmap bitmap) {
        imageBitmap = bitmap;
    }
    
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        imageUrl = args.getString(PARAM_IMAGE_URL);
        photoSender = args.getString(PARAM_PHOTO_SENDER);
        shouldAllowSaveToDevice = args.getBoolean(PARAM_ALLOW_SAVE_TO_DEVICE, true);
        isUrlForWebPage = args.getBoolean(PARAM_IS_URL_FOR_WEB_PAGE, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_photo_viewer;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image = (TouchImageView) view.findViewById(R.id.image);
        image.setOnClickListener(this);
        
        webView = (WebView) view.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setBackgroundColor(Color.BLACK);

        prgIndicator = (ProgressBar) view.findViewById(R.id.progressBar);
        if (imageBitmap != null) {
            image.setImageBitmap(imageBitmap);
            prgIndicator.setVisibility(View.GONE);

        } else if (!TextUtils.isEmpty(imageUrl)) {
            Logger.debug.log(LOG_TAG, "Displaying image with url:", imageUrl);
            ImageHandler.getInstance().loadImage(imageUrl, image, new ImageHandler.ImageLoadListener() {

                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    prgIndicator.setVisibility(View.GONE);
                    
                    image.setZoom(1);
                    
                    if (bitmap != null && isUrlForWebPage) {
                        final int width = Config.getInstance().getScreenWidth();
                        final int height = (int) (width * ((float) bitmap.getHeight() / bitmap.getWidth())); 
                        
                        RelativeLayout.LayoutParams webViewParams = (RelativeLayout.LayoutParams) webView.getLayoutParams();
                        webViewParams.width = width;
                        webViewParams.height = height;
                        webView.setLayoutParams(webViewParams);
                        webView.setVisibility(View.VISIBLE);
                        webView.loadDataWithBaseURL("file:///android_asset/","<html><center><img src=\"" + 
                        imageUrl + "\" width=\"100%\"></html>","text/html","utf-8","");
                    }
                }
                
                @Override
                public void onImageFailed(ImageView imageView) {
                    Tools.showToast(ApplicationEx.getContext(),
                            I18n.tr("We couldn't load the image. Try again."));
                }
            });
        } else {
            Tools.showToast(ApplicationEx.getContext(),
                    I18n.tr("We couldn't load the image. Try again."));
        }

        topControlsContainer = (RelativeLayout) view.findViewById(R.id.top_controls_container);
        bottomControlsContainer = (LinearLayout) view.findViewById(R.id.bottom_controls_container);

        imgClose = (ImageView) view.findViewById(R.id.img_close);
        imgClose.setOnClickListener(this);

        imgSave = (ImageView) view.findViewById(R.id.img_save);
        if (shouldAllowSaveToDevice) {
            imgSave.setOnClickListener(this);
            imgSave.setVisibility(View.VISIBLE);
        }

        imgShare = (ImageView) view.findViewById(R.id.img_share);
        imgShare.setOnClickListener(this);

        imgOptions = (ImageView) view.findViewById(R.id.img_options);
        // We hide the options image if the photo sender is the currently
        // logged in user because we should not be able to report abuse on
        // our own image.
        if (TextUtils.isEmpty(photoSender) || Session.getInstance().isSelfByUsername(photoSender)) {
            imgOptions.setVisibility(View.GONE);
        } else {
            imgOptions.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        final int actionId = v.getId();
        switch (actionId) {
            case R.id.image:
                onClickedPhotoImage();
                break;
            case R.id.img_close:
                onClickedImgClose();
                break;
            case R.id.img_save:
                onClickedImgSave();
                break;
            case R.id.img_share:
                onClickedImgShare();
                break;
            case R.id.img_options:
                onClickedImgOptions();
                break;
        }
    }

    private void onClickedPhotoImage() {
        boolean didPerformFade = setFadeAnimationOnControls(didControlsFadeOut);
        if (didPerformFade) {
            didControlsFadeOut = !didControlsFadeOut;
        }
    }

    private void onClickedImgClose() {
        if (!didControlsFadeOut) {
            AnimUtils.doClickScaleAnimation(imgClose, new AnimationListener() {
                
                @Override
                public void onAnimationStart(Animation animation) {}
                
                @Override
                public void onAnimationRepeat(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    closeFragment();
                }
            });            
        }
    }

    private void onClickedImgSave() {
        if (!didControlsFadeOut) {
            AnimUtils.doClickScaleAnimation(imgSave, null);
    
            ScheduledJobsHandler.getInstance().startJobWithKey(ScheduledJobKeys.SAVE_PHOTO_TO_EXTERNAL_STORAGE, new ScheduleListener() {

                @Override
                public void processJob() {
                    boolean didSavePhoto = StorageUtils.savePhotoToExternalStorage(ImageHandler.getBitmapFromImageView(image),
                            Bitmap.CompressFormat.PNG, Constants.SAVE_PHOTO_QUALITY);
                    if (!didSavePhoto) {
                        Tools.showToast(ApplicationEx.getContext(), I18n.tr("We couldn't save the image. Try again."));
                    } else {
                        Tools.showToast(ApplicationEx.getContext(), I18n.tr("Image saved"));
                    }
                }
            }, 1L, false);
        }
    }

    private void onClickedImgShare() {
        if (!didControlsFadeOut) {
            AnimUtils.doClickScaleAnimation(imgShare, null);
            
            // TODO Angelo: Implement share to miniblog / chat
            // functionality.
        }
    }

    private void onClickedImgOptions() {
        if (!didControlsFadeOut) {
            AnimUtils.doClickScaleAnimation(imgOptions, null);
    
            List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
            menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_abuse, null));
    
            Tools.showContextMenu(I18n.tr("Report"), menuItems, this);
        }
    }

    /**
     * Performs a fade-in or fade-out animation on the on-screen controls.
     * 
     * @param shouldFadeIn
     *            true if a fade-in animation should be performed and false if a
     *            fade-out animation should be performed.
     * @return true if the fade animation was done on any one of the controls
     *         and false if the fade animation was done on none of the controls.
     */
    private boolean setFadeAnimationOnControls(final boolean shouldFadeIn) {
        return (AnimUtils.doFadeAnimation(topControlsContainer, shouldFadeIn, null) | AnimUtils.doFadeAnimation(
                bottomControlsContainer, shouldFadeIn, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.listener.ContextMenuItemListener#onContextMenuItemClick
     * (com.projectgoth.model.ContextMenuItem)
     */
    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int optionId = menuItem.getId();
        switch (optionId) {
            case R.id.option_item_report_abuse:
                if (!TextUtils.isEmpty(photoSender)) {
                    ActionHandler.getInstance().displayBrowser(getActivity(),
                            String.format(WebURL.URL_REPORT_USER, photoSender));
                }
                break;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.pauseTimers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.resumeTimers();
        }
    }
}
