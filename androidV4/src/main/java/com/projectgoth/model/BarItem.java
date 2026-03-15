/**
 * Copyright (c) 2013 Project Goth
 * <p/>
 * BottomBarItem.java
 * Created 2 Jun, 2014, 6:57:42 pm
 */

package com.projectgoth.model;

import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.projectgoth.ui.widget.IconCounterView;

/**
 * @author warrenbalcos
 *
 */
public abstract class BarItem {

    protected FragmentActivity mActivity;

    /**
     * Default image icon
     */
    private int mIconRes;

    private String mTitle;

    /**
     * Use this to override the {@link #onPress()} method
     */
    private CustomAction mCustomAction;

    /**
     * image loader interface, take precedence over icon resource
     */
    private ImageLoader mImageLoader;

    public interface CustomAction {
        public void onPress();
    }

    public interface ImageLoader {

        public void load(ImageView view);

        public void load(IconCounterView view);
    }

    public BarItem(int iconRes) {
        super();
        setIconRes(iconRes);
    }

    /**
     * @return the iconRes
     */
    public int getIconRes() {
        return mIconRes;
    }

    /**
     * @param iconRes
     *            the iconRes to set
     */
    public void setIconRes(int iconRes) {
        if (iconRes <= 0) {
            throw new IllegalArgumentException("icon must be from android resource folder");
        }
        this.mIconRes = iconRes;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(FragmentActivity activity) {
        this.mActivity = activity;
    }

    /**
     * @return the imageLoader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * @param imageLoader the imageLoader to set
     */
    public void setImageLoader(ImageLoader imageLoader) {
        this.mImageLoader = imageLoader;
    }

    public void executeCustomAction() {
        if (mCustomAction != null) {
            mCustomAction.onPress();
        }
    }

    public boolean hasCustomAction() {
        return mCustomAction != null;
    }

    /**
     * Setting a {@link CustomAction} will override the {@link #onPress()}
     * action
     */
    public void setCustomAction(CustomAction customAction) {
        this.mCustomAction = customAction;
    }

    public abstract void onPress();

}
