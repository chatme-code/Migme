/**
 * Copyright (c) 2013 Project Goth
 * PagerSlidingTabHeader.java
 * Created Nov 28, 2014, 10:14:17 AM
 */

package com.projectgoth.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author mapet
 */
public class PagerSlidingTabHeader extends HorizontalScrollView {

    private final PageListener mPageListener = new PageListener();
    public OnPageChangeListener mDelegatePageListener;

    private LinearLayout mTabsContainer;
    private ViewPager mPager;

    private int mTabCount;
    private int mCurrentPosition = 0;
    private int mLastScrollX = 0;
    private final int SCROLL_OFFSET = 52;

    private LinearLayout.LayoutParams mExpandedTabLayoutParams;

    public PagerSlidingTabHeader(Context context) {
        this(context, null);
    }

    public PagerSlidingTabHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mTabsContainer);

        int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);
        int smallMargin = ApplicationEx.getDimension(R.dimen.small_margin);

        mExpandedTabLayoutParams = new LinearLayout.LayoutParams(ApplicationEx.getDimension(R.dimen.pager_tab_width),
                LinearLayout.LayoutParams.MATCH_PARENT);
        mExpandedTabLayoutParams.setMargins(smallMargin, normalMargin, smallMargin, normalMargin);
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mCurrentPosition = position;
            invalidate();

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(mPager.getCurrentItem(), 0, 0);
            }

            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageSelected(position);
            }
        }
    }

    public interface IconTabProvider {

        public int getPageIconResId(int position);

        public int getPageSelectedIconResId(int position);
    }

    private void scrollToChild(int position, int offset, int selectedResId) {
        if (mTabCount == 0) {
            return;
        }

        View tabView = mTabsContainer.getChildAt(position);
        if (mCurrentPosition == position) {
            tabView.setBackgroundResource(R.drawable.rounded_green_background);
            ((ImageView) tabView).setImageResource(selectedResId);
        }

        int newScrollX = mTabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= SCROLL_OFFSET;
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    public void setViewPager(ViewPager pager) {
        this.mPager = pager;

        if (pager.getAdapter() != null) {
            pager.setOnPageChangeListener(mPageListener);
            notifyDataSetChanged();
        }
    }

    public void setCurrentItem(final int index) {
        if (this.mPager != null) {
            this.mPager.setCurrentItem(index);
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mDelegatePageListener = listener;
    }

    private void addIconTab(final int position, int resId, int selectedResId) {
        ImageView tabIcon = new ImageView(getContext());
        tabIcon.setImageResource(resId);
        tabIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        tabIcon.setFocusable(true);

        tabIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(position);
            }
        });

        mTabsContainer.setGravity(Gravity.CENTER);
        mTabsContainer.addView(tabIcon, position, mExpandedTabLayoutParams);
    }

    public void notifyDataSetChanged() {
        mTabsContainer.removeAllViews();
        mTabCount = mPager.getAdapter().getCount();
        final IconTabProvider iconTabProvider = (IconTabProvider) mPager.getAdapter();

        for (int i = 0; i < mTabCount; i++) {
            if (mPager.getAdapter() instanceof IconTabProvider) {
                addIconTab(i, iconTabProvider.getPageIconResId(i), iconTabProvider.getPageSelectedIconResId(i));
            }
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                if (!UIUtils.hasJellyBean()) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                mCurrentPosition = mPager.getCurrentItem();
                scrollToChild(mCurrentPosition, 0, iconTabProvider.getPageSelectedIconResId(mCurrentPosition));
            }
        });
    }

}
