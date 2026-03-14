/**
 * Copyright (c) 2013 Project Goth
 *
 * AspectRatioImageView.java
 * Created May 20, 2014, 8:02:44 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * @author angelorohit
 *
 */
public class AspectRatioImageView extends ImageView {

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drw = getDrawable();
        if (null == drw || drw.getIntrinsicWidth() <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * drw.getIntrinsicHeight() / drw.getIntrinsicWidth();
            setMeasuredDimension(width, height);
        }
    }
}