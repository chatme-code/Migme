/**
 * Copyright (c) 2013 Project Goth
 *
 * CustomPopupActivity.java
 * Created May 21, 2014, 10:58:33 AM
 */

package com.projectgoth.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;

/**
 * @author mapet
 * 
 */
public class CustomPopupActivity extends BaseCustomFragmentActivity {

    private OnBackPressListener onBackPresslistener;
    private boolean             shouldShowTransition;
    public static final String  PARAM_SHOULD_SHOW_TRANSITION = "PARAM_SHOULD_SHOW_TRANSITION";

    public interface OnBackPressListener {

        public boolean onBackPress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Looks like add CustomPopupActivity to cache is useless, remove it

        setContentView(R.layout.activity_main);

        Intent params = getIntent();
        String fragmentId = params.getStringExtra(AppEvents.Application.Extra.FRAGMENT_ID);
        shouldShowTransition = params.getBooleanExtra(PARAM_SHOULD_SHOW_TRANSITION, false);

        if (shouldShowTransition) {
            showTransition(0, 0);
        }

        FragmentHandler.getInstance().showFragmentWithId(getSupportFragmentManager(), fragmentId, false, shouldShowTransition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.projectgoth.ui.activity.BaseFragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        FragmentHandler.getInstance().removeCustomPopupActivityFromCache(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQ_PIC_FROM_CAMERA_FOR_CHAT_MSG && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap photo = Tools.loadImageFromCapturedPhotoFile(this);
                if (photoEventListener != null) {
                    photoEventListener.onPhotoSendPhoto(photo);
                }
            } catch (Exception e) {
            }

        } else if (requestCode == Constants.REQ_PIC_FROM_GALLERY_FOR_CHAT_MSG && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                Uri selectedImage = intent.getData();

                try {
                    Bitmap resizedBitmap = Tools.resizeAndRotateImage(this, selectedImage,
                            Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);

                    if (photoEventListener != null) {
                        photoEventListener.onPhotoSendPhoto(resizedBitmap);
                    }

                } catch (Exception e) {
                }
            }
        }
        // the request is sent from the photo album pagelet which a nested
        // BrowserFragment in ProfileFragment.
        // there is an issue that nested Fragment may not be able to receive the
        // onActivityResult, so I handle it here
        else if (requestCode == Constants.REQ_PIC_FROM_CAMERA_FOR_PHOTO_ALBUM && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap photo = Tools.loadImageFromCapturedPhotoFile(this);
                // no need to resize again here, it's already resized when it's
                // loaded previously, which is good for memory usage
                byte[] imageData = Tools.getBitmapDataForUpload(photo, false);

                Tools.showToast(this, I18n.tr("Uploading"));

                UserDatastore.getInstance().requestUploadPhotoToPhotoAlbum(imageData);
                photo.recycle();

            } catch (Exception e) {

            }
        } else if (requestCode == Constants.REQ_PIC_FROM_GALLERY_FOR_PHOTO_ALBUM && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                Uri selectedImage = intent.getData();

                try {
                    Bitmap selectedBmp = null;
                    selectedBmp = Tools.resizeAndRotateImage(this, selectedImage, Constants.DEFAULT_PHOTO_SIZE,
                            Constants.DEFAULT_PHOTO_SIZE);
                    // no need to resize again here, it's already resized when
                    // it's loaded previously, which is good for memory usage
                    byte[] imageData = Tools.getBitmapDataForUpload(selectedBmp, false);

                    Tools.showToast(this, I18n.tr("Uploading"));

                    UserDatastore.getInstance().requestUploadPhotoToPhotoAlbum(imageData);
                    selectedBmp.recycle();

                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        pushContentFrameUp();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onBackPresslistener != null) {
                if (onBackPresslistener.onBackPress()) {
                    return true;
                }
            }
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setOnBackPresslistener(OnBackPressListener listener) {
        this.onBackPresslistener = listener;
    }

    @Override
    public void finish() {
        super.finish();
        if (shouldShowTransition) {
            showTransition(0, 0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent params = intent;
        String fragmentId = params.getStringExtra(AppEvents.Application.Extra.FRAGMENT_ID);
        shouldShowTransition = params.getBooleanExtra(PARAM_SHOULD_SHOW_TRANSITION, false);

        if (shouldShowTransition) {
            showTransition(0, 0);
        }

        FragmentHandler.getInstance().showFragmentWithId(getSupportFragmentManager(), fragmentId, false, shouldShowTransition);
    }
}
