package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by houdangui on 20/10/14.
 * a scroll view with max height
 */
public class ScrollViewEx extends ScrollView{
    
    public interface TouchEventsListener {
        public void onInterceptTouchEvent(MotionEvent event);
    }

    private int maxHeight = -1;
    private TouchEventsListener touchEventsListener;

    public ScrollViewEx(Context context) {
        super(context);
    }

    public ScrollViewEx(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ScrollViewEx(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight >= 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
    
    public void setTouchEventsListener(final TouchEventsListener listener) {
        this.touchEventsListener = listener;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (touchEventsListener != null) {
            touchEventsListener.onInterceptTouchEvent(event);
        }
        return super.onInterceptTouchEvent(event);
    }
}
