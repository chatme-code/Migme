package com.projectgoth.ui.widget.allaccessbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import com.projectgoth.R;
import com.projectgoth.ui.animation.RotateAndTranslateAnimation;

public class ContextActionsLayout extends CollapsibleLayout {

    protected static final Interpolator defaultExpandCollapseItemsBaseInterpolator = new LinearInterpolator();
    protected static final Interpolator defaultExpandItemsRotationInterpolator = new DecelerateInterpolator();
    protected static final Interpolator defaultCollapseItemsRotationInterpolator = defaultExpandCollapseItemsBaseInterpolator;
    protected static final Interpolator defaultExpandItemsTranslationInterpolator = new OvershootInterpolator(2.0f);
    protected static final Interpolator defaultCollapseItemsTranslationInterpolator = new AnticipateInterpolator(2.5f) {
        private static final float delay = 0.18f;
        private static final float multiplier = 1f / (1f - delay); 
        @Override
        public float getInterpolation(float input) {
            // Add a small delay
            return super.getInterpolation(input<delay? 0f : (input-delay)*multiplier);
        }
    };
    protected static final Animation animFadeAway = new AlphaAnimation(1.0f, 0.0f);
    protected static final Interpolator fadeInterpolator = new DecelerateInterpolator();
    
    public ContextActionsLayout(Context context) {
        super(context);
        init();
    }

    public ContextActionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundResourceId = R.drawable.all_access_button_context_shape;
        expandItemsAnimLength = 450;
        collapseItemsAnimLength = 400;
        
        layoutPadding += itemSize;
    }
    
    @Override
    protected Animation createItemClickAnimations(int duration, boolean isClicked) {
        float scaleTo = isClicked? 2f : 0f;
        Animation scaleAnim = new ScaleAnimation(1f, scaleTo, 1f, scaleTo,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(animFadeAway);
        animationSet.setDuration(duration);
        animationSet.setInterpolator(fadeInterpolator);
        animationSet.setFillAfter(true);
        
        return animationSet;
    }

    @Override
    protected Animation createExpandAnimation(float fromX, float toX, float fromY, float toY) {
        RotateAndTranslateAnimation animation = new RotateAndTranslateAnimation(fromX, toX, fromY, toY, 90, -360);
        animation.setDuration(expandItemsAnimLength);
        animation.setInterpolator(defaultExpandCollapseItemsBaseInterpolator);
        animation.setTranslationInterpolator(defaultExpandItemsTranslationInterpolator);
        animation.setRotationInterpolator(defaultExpandItemsRotationInterpolator);
        animation.setFillAfter(true);
        return animation;
    }
    
    @Override
    protected Animation createCollapseAnimation(float fromX, float toX, float fromY, float toY) {
        RotateAndTranslateAnimation animation = new RotateAndTranslateAnimation(fromX, toX, fromY, toY, 0, 540);
        animation.setDuration(collapseItemsAnimLength);
        animation.setInterpolator(defaultExpandCollapseItemsBaseInterpolator);
        animation.setTranslationInterpolator(defaultCollapseItemsTranslationInterpolator);
        animation.setRotationInterpolator(defaultCollapseItemsRotationInterpolator);
        animation.setFillAfter(true);
        return animation;
    }

}
