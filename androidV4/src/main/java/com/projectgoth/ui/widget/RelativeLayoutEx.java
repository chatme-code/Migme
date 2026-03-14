
package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.projectgoth.common.Config;
import com.projectgoth.listener.OnSizeChangedListener;
import com.projectgoth.ui.listener.KeyboardListener;

public class RelativeLayoutEx extends RelativeLayout {

    KeyboardListener listener = null;
    
    private OnSizeChangedListener onSizeChangedListener = null;

    public RelativeLayoutEx(Context context) {
        super(context);
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        //android has no api to detect if soft keyboard shown or hidden, this is a workaround
        if (listener != null) {
            if (actualHeight > proposedheight) {
                int keyboardHeight = actualHeight - proposedheight;
                Config.getInstance().setSoftKeyboardHeight(keyboardHeight);
                listener.onSoftKeyboardShown();
            } else if (actualHeight < proposedheight) {
                listener.onSoftKeyboardHidden();
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (onSizeChangedListener != null) {
            onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }
    
    public void setOnSizeChangedListener(final OnSizeChangedListener listener) {
        this.onSizeChangedListener = listener;
    }
}
