/**
 * Copyright (c) 2013 Project Goth
 *
 * InfiniteViewPager.java
 * Created Sep 3, 2014, 2:05:48 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.projectgoth.ui.adapter.InfiniteViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A custom view pager that supports infinite scrolling.
 * Swipe events are disabled for this view pager.
 * @author angelorohit
 */
public class InfiniteViewPager extends ViewPager {

    private final List<OnPageChangeListener> externalOnPageChangeListeners = new ArrayList<OnPageChangeListener>();
    private final ViewPagerListener viewPagerListener = new ViewPagerListener();
    
    private boolean allowSwiping = true;
    private boolean isTransitioningBetweenPages = false;
    private boolean isProcessingOnScrollFinished = false;
    
    private int pageScrollState = SCROLL_STATE_IDLE;
    
    
    public InfiniteViewPager(Context context) {
        super(context);
        init();
    }

    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setActualOnPageChangeListener(viewPagerListener);
    }
    
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        isTransitioningBetweenPages = smoothScroll;
        super.setCurrentItem(item, smoothScroll);
    }
    
    public void setAllowSwiping(boolean allow) {
        allowSwiping = allow;
    }

    public boolean getAllowSwiping() {
        return allowSwiping;
    }

    private void setActualOnPageChangeListener(final OnPageChangeListener listener) {
        super.setOnPageChangeListener(listener);
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        externalOnPageChangeListeners.add(listener);
    }
    
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        externalOnPageChangeListeners.remove(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return allowSwiping && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return allowSwiping && super.onTouchEvent(event);
    }
    
    @Override
    public void endFakeDrag() {
        super.endFakeDrag();

        // Ending a drag can result in 2 cases:
        // - needs to further scroll to the nearest page
        // - pager is already fully scrolled
        if (pageScrollState == SCROLL_STATE_IDLE) {
            // If no further scroll is needed, then cycle pages immediately
            onScrollFinished();
        } else {
            // Otherwise defer the page cycling to onPageScrollStateChanged
            isTransitioningBetweenPages = true;
        }
    }
    
    public boolean isScrolling() {
        return pageScrollState != SCROLL_STATE_IDLE;
    }
    
    private void onScrollFinished() {
        final InfiniteViewPagerAdapter adapter = (InfiniteViewPagerAdapter) getAdapter();
        isProcessingOnScrollFinished = true;
        final int position = getCurrentItem();
        int cycleResult = adapter.cycle(position);
        if (cycleResult != 0) {
            setCurrentItem(position + cycleResult, false);
        }
        isProcessingOnScrollFinished = false;

    }

    private final class ViewPagerListener implements OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            for (OnPageChangeListener listener : externalOnPageChangeListeners) {
                listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (pageScrollState != SCROLL_STATE_SETTLING && !isFakeDragging()) {
                for (OnPageChangeListener listener : externalOnPageChangeListeners) {
                    listener.onPageSelected(position);
                }
            }
            
            if (pageScrollState == SCROLL_STATE_IDLE &&
                !isProcessingOnScrollFinished &&
                !isFakeDragging())
            {
                // Cycle the pages once the pager has changed page and no scrolling is
                // happening. This needs to be done after this call, so we post-delay it.
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onScrollFinished();
                    }
                }, 0);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Need to update the state ALWAYS
            pageScrollState = state;
            
            for (OnPageChangeListener listener : externalOnPageChangeListeners) {
                listener.onPageScrollStateChanged(state);
            }
            
            if (pageScrollState == SCROLL_STATE_IDLE &&
                !isProcessingOnScrollFinished && 
                (allowSwiping || isTransitioningBetweenPages)) {

                isTransitioningBetweenPages = false;
                // Once the pager has stopped scrolling we need to cycle the pager items.
                // This needs to be done after this call, so we post-delay it.
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onScrollFinished();
                    }
                }, 0);
            }
        }
    }
}
