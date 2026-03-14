/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuBarAnimation.java
 * Created Nov 25, 2014, 11:18:15 AM
 */

package com.projectgoth.ui.activity;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.projectgoth.util.AnimUtils;

/**
 * @author warrenbalcos
 * 
 */
public class MenuBarAnimation {

    private final long   ANIMATION_SPEED    = 350;

    /**
     * time delay in milliseconds to lock the view as shown
     */
    private final int    DEFAULT_SHOWN_LOCK = 500;

    private int          showLock           = DEFAULT_SHOWN_LOCK;

    private long         showLockExpiry;

    private View         view;

    private Animation    hideAnimation;

    private Animation    showAnimation;

    private MenuBarState state;
    private boolean      isAnimating;
    private boolean      isShown;

    private enum MenuBarState {
        HIDDEN, SHOWN;
    }

    public enum AnimationType {
        SLIDE_TO_TOP, SLIDE_TO_BOTTOM, SLIDE_FROM_TOP, SLIDE_FROM_BOTTOM;
    }

    public MenuBarAnimation(View view) {
        setView(view);
        init();
    }

    private void init() {
        state = MenuBarState.SHOWN;
        isAnimating = false;
        isShown = true;
    }

    //@formatter:off
    private AnimationListener showListener = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {
            isAnimating = true;
            view.setVisibility(View.VISIBLE);
        }
        
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            isAnimating = false;
            isShown = true;
            showLockExpiry = System.currentTimeMillis();
            processState();
        }
    };
    
    private AnimationListener hideListener = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {
            isAnimating = true;
            isShown = false;
        }
        
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            isAnimating = false;
            showLockExpiry = System.currentTimeMillis();
            view.setVisibility(View.GONE);
            processState();
        }
    };
    //@formatter:on

    private void processState() {
        if (!isAnimating) {
            switch (state) {
                case SHOWN:
                    if (!isShown()) {
                        view.startAnimation(showAnimation);
                    }
                    break;
                case HIDDEN:
                    if (isShown()) {
                        view.startAnimation(hideAnimation);
                    }
                    break;
            }
        }
    }

    /**
     * @return the isShown
     */
    public boolean isShown() {
        return isShown;
    }

    public void show() {
        if (showLock == 0 || (showLockExpiry + showLock) < System.currentTimeMillis()) {
            if (state != MenuBarState.SHOWN && !isAnimating) {
                state = MenuBarState.SHOWN;
                processState();
            }
        }
    }

    public void hide() {
        if (showLock == 0 || (showLockExpiry + showLock) < System.currentTimeMillis()) {
            if (state != MenuBarState.HIDDEN && !isAnimating) {
                state = MenuBarState.HIDDEN;
                processState();
            }
        }
    }

    public void setShowLockLength(int length) {
        showLock = length;
    }

    public int getShowLockLength() {
        return showLock;
    }

    public void setShowAnimation(AnimationType type) {
        showAnimation = getAnimation(type, showListener);
    }

    public void setHideAnimation(AnimationType type) {
        hideAnimation = getAnimation(type, hideListener);
    }

    private Animation getAnimation(AnimationType type, AnimationListener listener) {
        if (type == null) {
            throw new IllegalArgumentException("AnimationType cannot be null");
        }

        Animation anim = null;

        switch (type) {
            case SLIDE_TO_BOTTOM:
                anim = AnimUtils.createSlideToBottomAnimation(view, ANIMATION_SPEED, listener);
                break;
            case SLIDE_FROM_BOTTOM:
                anim = AnimUtils.createSlideFromBottomAnimation(view, ANIMATION_SPEED, listener);
                break;
            case SLIDE_FROM_TOP:
                anim = AnimUtils.createSlideFromTopAnimation(view, ANIMATION_SPEED, listener);
                break;
            case SLIDE_TO_TOP:
                anim = AnimUtils.createSlideToTopAnimation(view, ANIMATION_SPEED, listener);
                break;
        }
        return anim;
    }
    
    public void setView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        this.view = view;
    }

}
