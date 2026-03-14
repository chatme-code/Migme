/**
 * Copyright (c) 2013 Project Goth
 *
 * HorizontalListViewEx.java
 * Created Jul 11, 2013, 4:52:32 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.projectgoth.R;

/**
 * @author mapet
 * 
 */
public class HorizontalListViewEx extends HorizontalScrollView implements View.OnClickListener {

    private LinearLayout        mItemContainer;
    private Adapter             mAdapter;
    private OnItemClickListener mItemSelectedListener;
    private int                 mItemDisplayCount = -1;

    public interface OnItemClickListener {
        public void onItemClicked(HorizontalListViewEx adapterView, View view, int position, long id);
    }

    public HorizontalListViewEx(Context context) {
        this(context, null);
    }

    public HorizontalListViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalListViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mItemContainer = new LinearLayout(context);
        HorizontalScrollView.LayoutParams lp = new HorizontalScrollView.LayoutParams(LayoutParams.WRAP_CONTENT,
                getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_icon_size));
        mItemContainer.setLayoutParams(lp);
        mItemContainer.setGravity(Gravity.CENTER_VERTICAL);
        mItemContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mItemContainer);
    }

    public void setItemLayoutParams(HorizontalScrollView.LayoutParams lp) {
        mItemContainer.setLayoutParams(lp);
    }

    public void setDisplayMaxCount(int count) {
        mItemDisplayCount = count;
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mItemContainer.removeAllViews();
        int count = mItemDisplayCount > 0 ? mItemDisplayCount : mAdapter.getCount();
        for (int position = 0; position < count; position++) {
            View view = mAdapter.getView(position, null, null);
            view.setOnClickListener(this);
            view.setTag(Integer.valueOf(position));
            mItemContainer.addView(view);
        }
    }
 
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemSelectedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mItemSelectedListener != null) {
            Integer position = (Integer) v.getTag();
            if (position != null) {
                mItemSelectedListener.onItemClicked(this, v, position, mAdapter.getItemId(position));
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

}
