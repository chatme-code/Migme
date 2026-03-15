/**
 * Copyright (c) 2013 Project Goth
 *
 * ResizeAnimation.java
 * Created 23 Sep, 2014, 9:56:43 am
 */

package com.projectgoth.ui.animation;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.projectgoth.util.MathUtils;


/**
 * @author michaeljoos
 *
 */
public class ResizeAnimation extends Animation {
    protected View view; 
    protected int fromWidth;
    protected int toWidth;
    protected int fromHeight;
    protected int toHeight;

    public ResizeAnimation(View v, int toWidth, int toHeight) {
        this.view = v;
        this.fromWidth = v.getWidth();
        this.fromHeight = v.getHeight();
        this.toWidth = toWidth;
        this.toHeight = toHeight;
    }

    public ResizeAnimation(View v, int fromWidth, int toWidth, int fromHeight, int toHeight) {
        this.view = v;
        this.fromWidth = fromWidth;
        this.toWidth = toWidth;
        this.fromHeight = fromHeight;
        this.toHeight = toHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LayoutParams params = view.getLayoutParams();
        params.width = MathUtils.lerp(fromWidth, toWidth, interpolatedTime);
        params.height = MathUtils.lerp(fromHeight, toHeight, interpolatedTime);
        view.requestLayout();
    }
}
