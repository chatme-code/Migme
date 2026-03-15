/**
 * Copyright (c) 2013 Project Goth
 *
 * LinearLayoutEx.java
 * Created Jul 31, 2013, 6:04:25 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.ui.listener.KeyboardListener;

/**
 * @author mapet
 * 
 */

public class LinearLayoutEx extends LinearLayout {

    private static final int KEYBOARD_THRESHOLD = 150;

    private boolean isKeyboardShowing = false;

    public static class LayoutParams extends LinearLayout.LayoutParams {
        boolean autoHideWhenSoftKeyboardPresent;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.LinearLayoutEx_LayoutParams);

            autoHideWhenSoftKeyboardPresent = a.getBoolean(R.styleable.LinearLayoutEx_LayoutParams_layout_autoHideWhenSoftKeyboardPresent, false);

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight, boolean autoHide) {
            super(width, height, weight);
            this.autoHideWhenSoftKeyboardPresent = autoHide;
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LinearLayout.LayoutParams p) {
            super(p);
        }

    }

    private KeyboardListener listener = null;

    public LinearLayoutEx(Context context) {
        super(context);
    }

    public LinearLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }


    private void setAutoVisibility(int newVisibility) {
        final int count = getChildCount();

        for(int i = 0; i < count; ++i) {
            final View child = getChildAt(i);

            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if(p.autoHideWhenSoftKeyboardPresent) {
                child.setVisibility(newVisibility);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        LinearLayout.LayoutParams p = super.generateDefaultLayoutParams();
        if(p != null) {
            return new LayoutParams(p);
        }
        return null;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LinearLayoutEx.LayoutParams;
    }


    private void onKeyboardShown() {
        if(listener != null) listener.onSoftKeyboardShown();
        setAutoVisibility(View.GONE);
    }

    private void onKeyboardHidden() {
        if(listener != null) listener.onSoftKeyboardHidden();
        setAutoVisibility(View.VISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int currentHeight = getHeight();
        final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(currentHeight != 0) {
            if(currentHeight > proposedHeight + KEYBOARD_THRESHOLD) {
                if(isKeyboardShowing == false) {
                    isKeyboardShowing = true;
                    int keyboardHeight = currentHeight - proposedHeight;
                    Config.getInstance().setSoftKeyboardHeight(keyboardHeight);
                    onKeyboardShown();
                }
            }
            else if(currentHeight < proposedHeight - KEYBOARD_THRESHOLD) {
                if(isKeyboardShowing == true) {
                    isKeyboardShowing = false;
                    onKeyboardHidden();
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
