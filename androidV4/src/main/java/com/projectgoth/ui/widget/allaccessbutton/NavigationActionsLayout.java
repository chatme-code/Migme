package com.projectgoth.ui.widget.allaccessbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import com.projectgoth.R;

public class NavigationActionsLayout extends CollapsibleLayout {

    private static final Interpolator defaultExpandCollapseInterpolator = new AccelerateDecelerateInterpolator();
    
    private static final long expandItemsAnimDelay = 200;
    
    public NavigationActionsLayout(Context context) {
        super(context);
        init();
    }

    public NavigationActionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        backgroundResourceId = R.drawable.all_access_button_navigation_shape;
        expandItemsAnimLength = 200;
        collapseItemsAnimLength = 150;
    }

    @Override
    protected float calculateAngleBetweenItems() {
        return (float) Math.toRadians(180f);
    }
    
    @Override
    protected Animation createItemClickAnimations(int duration, boolean isClicked) {
        // Since navigation buttons are not clickable, we return null to avoid having
        // an unused animation
        return null;
    }

    @Override
    protected Animation createExpandAnimation(float fromX, float toX, float fromY, float toY) {
        return createExpandCollapseAnimation(fromX, toX, fromY, toY, expandItemsAnimDelay,
                expandItemsAnimLength, defaultExpandCollapseInterpolator);
    }
    
    @Override
    protected Animation createCollapseAnimation(float fromX, float toX, float fromY, float toY) {
        return createExpandCollapseAnimation(fromX, toX, fromY, toY, 0,
                collapseItemsAnimLength, defaultExpandCollapseInterpolator);
    }
    
    private static Animation createExpandCollapseAnimation(float fromX, float toX, float fromY, float toY,
            long delay, long duration, Interpolator interpolator) {
    
        Animation animation = new TranslateAnimation(fromX, toX, fromY, toY);
        animation.setStartOffset(delay);
        animation.setDuration(duration);
        animation.setInterpolator(interpolator);
        animation.setFillAfter(true);
        return animation;
    }
}
