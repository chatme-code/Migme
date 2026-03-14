/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionBarConfig.java
 * Created Jul 25, 2013, 12:22:30 PM
 */

package com.projectgoth.ui.activity;

import android.graphics.Bitmap;
import com.projectgoth.common.Tools;

/**
 * @author cherryv
 * 
 */
public class CustomActionBarConfig {

    private CustomActionBarOverflowListener     mActionBarOverflowListener;
    
    public enum OverflowButtonState {
        NONE, POPUP, ALERT; 
    }

    public enum NavigationButtonState {
        HANDBURGUER, BACK; 
    }

    private NavigationButtonState navigationButtonState = NavigationButtonState.HANDBURGUER;
    private OverflowButtonState overflowButtonState = OverflowButtonState.NONE;
    
    /**
     * Flag to determine if we want the search button displayed on the left of
     * the overflow button (if any) at the right-most side of the action bar.
     */
    private boolean showSearchButton      = false;
    
    private boolean showUnreadCountOnBack = false;

    /**
     * Reference to the customView layout that will be displayed in between the
     * home button and overflow button. If -1, then we use the default layout
     */
    private int customViewLayoutSrc  = -1;    

    /**
     * Can be used to override the default overflow icon
     */
    private Bitmap  overflowIcon;
    private int     overflowIconId;

    public interface CustomActionBarOverflowListener {
        public void onOverflowIconClicked();
    }
    
    public CustomActionBarConfig() {}
    
    public CustomActionBarConfig(CustomActionBarConfig other) {
        navigationButtonState = other.navigationButtonState;
        overflowButtonState = other.overflowButtonState;
        customViewLayoutSrc = other.customViewLayoutSrc;
        overflowIcon = other.overflowIcon;
    }

    public boolean isShowSearchButton() {
        return showSearchButton;
    }

    public void setShowSearchButton(boolean showSearchButton) {
        this.showSearchButton = showSearchButton;
    }

    public NavigationButtonState getNavigationButtonState() {
        return navigationButtonState;
    }

    public void setNavigationButtonState(NavigationButtonState state) {
        navigationButtonState = state;
    }
    
    public OverflowButtonState getOverflowButtonState() {
        return overflowButtonState;
    }

    public void setShowOverflowButtonState(OverflowButtonState state) {
        overflowButtonState = state;
    }
    
    public int getCustomViewLayoutSrc() {
        return customViewLayoutSrc;
    }

    public void setCustomViewLayoutSrc(int customViewLayoutSrc) {
        this.customViewLayoutSrc = customViewLayoutSrc;
    }
    
    public Bitmap getOverflowIcon() {
        return this.overflowIcon;
    }

    public int getOverflowIconId() {
        return this.overflowIconId;
    }

    public void setOverflowIcon(Bitmap bitmap) {
        if (bitmap != null) {
            this.overflowIcon = bitmap;
        }
    }

    public void onActionBarOverflowIconClicked() {
        if(mActionBarOverflowListener != null) {
            mActionBarOverflowListener.onOverflowIconClicked();
        }
    }

    public void setActionBarOverflowListener(CustomActionBarOverflowListener listener) {
        this.mActionBarOverflowListener = listener;
    }
    
    public void setOverflowIcon(int resId) {
        this.overflowIconId = resId;
        Bitmap bitmap = Tools.getBitmap(resId);
        setOverflowIcon(bitmap);
    }
    
    public boolean isShowUnreadCountOnBack() {
        return showUnreadCountOnBack;
    }

    public void setShowUnreadCountOnBack(boolean showUnreadCountOnBack) {
        this.showUnreadCountOnBack = showUnreadCountOnBack;
    }
    
}
