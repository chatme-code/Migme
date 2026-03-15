/**
 * Copyright (c) 2013 Project Goth
 *
 * NavigationSelectorAnimator.java
 * Created 26 Sep, 2014, 11:36:51 am
 */

package com.projectgoth.ui.widget.allaccessbutton;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.projectgoth.util.MathUtils;


/**
 * @author michaeljoos
 *
 */
public class NavigationSelectorAnimator extends ValueAnimator {

    private static final Interpolator defaultInterpolator = new AccelerateDecelerateInterpolator();
    private final InternalAnimatorListener animListener = new InternalAnimatorListener();

    private final View navigationSelector;

    private boolean isReversingAnimation = false;
    private float collapsedSize = 0;
    private float expandedSize = 0;
    private float reversedStartX = 0;
    
    public NavigationSelectorAnimator(View selector, float... values) {
        super();
        
        navigationSelector = selector;
        
        setFloatValues(values);
        setInterpolator(defaultInterpolator);
        
        addListener(animListener);
        addUpdateListener(animListener);
    }
    
    @Override
    public void start() {
        isReversingAnimation = false;
        ViewHelper.setTranslationX(navigationSelector, 0);
        super.start();
    }

    @Override
    public void reverse() {
        if (navigationSelector.getVisibility() != View.VISIBLE) {
            cancel();
        } else {
            if (getAnimatedFraction() < 1f) {
                setStartDelay(0);
            }
            isReversingAnimation = true;
            reversedStartX = ViewHelper.getTranslationX(navigationSelector);
            super.reverse();
        }
    }

    public boolean isReversing() {
        return isReversingAnimation;
    }

    public void setCollapsedSize(float size) {
        collapsedSize = size;
    }

    public void setExpandedSize(float size) {
        expandedSize = size;
    }

    class InternalAnimatorListener implements AnimatorListener, ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationStart(Animator animation) {
            if (navigationSelector.getVisibility() != View.VISIBLE) {
                navigationSelector.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            if (navigationSelector.getLayoutParams().width == collapsedSize &&
                navigationSelector.getVisibility() != View.GONE) {

                navigationSelector.setVisibility(View.GONE);
            }
            navigationSelector.setAnimation(null);
        }
        
        @Override
        public void onAnimationRepeat(Animator animation) {}

        @Override
        public void onAnimationCancel(Animator animation) {}

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float t = (Float) animation.getAnimatedValue();
            LayoutParams params = navigationSelector.getLayoutParams();
            params.width = params.height = (int)MathUtils.lerp(collapsedSize, expandedSize, t);
            if (isReversingAnimation) {
                // When collapsing, need to move the selector from wherever it is to the center
                ViewHelper.setTranslationX(navigationSelector, MathUtils.lerp(0, reversedStartX, t));
            }
            navigationSelector.requestLayout();
        }
    }
}
