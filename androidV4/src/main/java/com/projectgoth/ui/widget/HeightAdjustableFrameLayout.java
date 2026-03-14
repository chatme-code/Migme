package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class HeightAdjustableFrameLayout extends FrameLayout {
    private int heightAdjust = 0;
    private int measuredHeight = 0;

    public HeightAdjustableFrameLayout(Context context) {
        super(context);
    }

    public HeightAdjustableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(heightAdjust != 0) {
            int measuredWidth = super.getMeasuredWidth();
            measuredHeight = super.getMeasuredHeight();

            int newHeight = measuredHeight + heightAdjust;
            if(newHeight < 0) newHeight = 0;

            setMeasuredDimension(measuredWidth, newHeight);
        }
    }

    public int getHeightAdjust() {
        return heightAdjust;
    }
    public int getVisibleHeightAdjust() {
        if(measuredHeight + heightAdjust < 0) return -measuredHeight;
        return heightAdjust;
    }

    public void setHeightAdjust(int adjust) {
        if (heightAdjust != adjust) {
            heightAdjust = adjust;
            int newHeight = measuredHeight + adjust;
            if(newHeight < 0) newHeight = 0;
            if (newHeight != getMeasuredHeight()) {
                requestLayout();
            }
        }
    }
}
