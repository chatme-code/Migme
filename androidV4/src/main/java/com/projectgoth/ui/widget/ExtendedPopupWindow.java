/**
 * Copyright (c) 2013 Project Goth
 *
 * ExtendedPopupWindow.java
 * Created Jul 9, 2013, 4:37:02 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;


/**
 * @author cherryv
 * 
 */
public abstract class ExtendedPopupWindow implements PopupWindow.OnDismissListener {

    protected PopupWindow mWindow;
    protected View        mAnchor;
    protected int         mGravity;
    protected int         xOffset;
    protected int         yOffset;
    protected boolean     isShown;
    public Rect           mAnchorRect;

    public ExtendedPopupWindow(Context context) {
        this(context, null);
    }

    public ExtendedPopupWindow(Context context, View anchor) {
        this.mWindow = new PopupWindow(context);
        this.setPopupAnchor(anchor);
        isShown = false;
        setupPopup();

        this.mWindow.setOnDismissListener(this);
        onPopupCreated();
    }
    
    /**
     * Gets the X offset at which the popup window will be shown.
     * @return {@link #xOffset}
     */
    public int getXOffset() {
        return xOffset;
    }
    
    /**
     * Gets the Y offset at which the popup window will be shown.
     * @return {@link #yOffset}
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Initializes the popup window for display.
     * 
     * By default, width and height are based on #
     * {@link LayoutParams#WRAP_CONTENT}. Apparently, these are the only values
     * of the width and height where we can make use of
     * {@link PopupWindow#setBackgroundDrawable(android.graphics.drawable.Drawable)}
     * . An alternative is to set the background of the root viewgroup and set
     * the {@link #mWindow} background to an empty {@link BitmapDrawable}.
     */
    protected void setupPopup() {
        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        // To fix crash issue [#57704898], the solution is to set the
        // backgroundDrawable null here
        // and handle the back key event of dismissing the window from setting
        // the listener to
        // the root view of the window, see the example in PopupMenu constructor
        mWindow.setBackgroundDrawable(null);
    }
    
    public void setPopupWidth(int width) {
        mWindow.setWidth(width);
    }

    @Override
    public void onDismiss() {
        isShown = false;
        onPopupDismissed();
    }

    /**
     * Callback method to be called when this is created. Can contain additional
     * initialization for extending classes.
     */
    protected abstract void onPopupCreated();

    /**
     * Callback method called when popup is dismissed. Can be used to release
     * all referenced resources.
     */
    protected abstract void onPopupDismissed();

    /**
     * Method to be called by outside classes to display this popup
     */
    public void show(final boolean shouldAnimate) {
        showPopup(shouldAnimate);
        this.mAnchor.setVisibility(View.VISIBLE);
        isShown = true;
    }
    
    /**
     * Shows the popup window at the given x, y location.
     * @param x The X offset at which the window should be shown.
     * @param y The Y offset at which the window should be shown.
     * @param shouldAnimate true to animate the opening and closing of the window and false if no animation is to be 
     * performed.
     */
    public void showAtLocation(final int x, final int y, final boolean shouldAnimate) {
        setXYOffset(x, y);
        show(shouldAnimate);
    }
    
    /**
     * Specific implementation of subclasses on how the popup should be
     * rendered. Called internally by {@link #show()}
     * @param shouldAnimate Indicates whether the pop should animate or be shown immediately without animation.
     */
    protected abstract void showPopup(final boolean shouldAnimate);

    /**
     * Method to be called to close or dismiss this popup
     */
    public void dismiss() {
        if (mWindow != null) {
            this.mAnchor.setVisibility(View.INVISIBLE);
            mWindow.dismiss();
        }
    }

    /**
     * Sets to which view this popup will be anchored to
     * 
     * @param anchor
     *            View to anchor the popup with
     */
    public void setPopupAnchor(View anchor) {
        this.mAnchor = anchor;
        getAnchorRect();
    }

    protected void getAnchorRect() {
        if (this.mAnchor != null) {
            int[] location = new int[2];

            mAnchor.getLocationOnScreen(location);
            mAnchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1]
                    + mAnchor.getHeight());
        }
    }

    public void setPopupGravity(int gravity) {
        mGravity = gravity;
    }

    public void setXYOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public boolean isShown() {
        return isShown;
    }

    // call this after setting the content view in child class
    public void setCanceledOnKeyPress(final int[] hotkeySet) {
        if (mWindow.getContentView() == null)
            return;

        // this is necessary to make the OnKeyListener work
        mWindow.getContentView().setFocusableInTouchMode(true);

        // To fix crash issue [#57704898], we handle the back key event of
        // dismissing the window here
        // instead of the default behavior
        // close the window when menu key and back key triggered
        mWindow.getContentView().setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean isHotKey = false;
                if (hotkeySet != null) {
                    for (int i = 0; i < hotkeySet.length; i++) {
                        if (keyCode == hotkeySet[i]) {
                            isHotKey = true;
                        }
                    }
                }

                if ((isHotKey && event.getAction() == KeyEvent.ACTION_DOWN)
                        || (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
                        && mWindow.isShowing()) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    public void setCanceledOnTouchOutside() {
        // close the window when a touch even happens outside of the window
        mWindow.getContentView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && mWindow.isShowing()) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

}
