/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatInputTabs.java
 * Created 19 Mar, 2014, 11:10:37 am
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;


/**
 * @author Dangui
 *
 */
public class DrawerTabs extends LinearLayout implements View.OnClickListener {

    public interface OnTabClickListener {
        public void onTabClicked(View view, int position, long id);
    }

    private Adapter mAdapter;
    private OnTabClickListener onTabClickListener;
    private Context mContext;
    private int mDividerColor = Theme.getColor(ThemeValues.TAB_DIVIDER_COLOR);
    
    /**
     * @param context
     */
    public DrawerTabs(Context context) {
       this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public DrawerTabs(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    /**
     * @param mAdapter the mAdapter to set
     */
    public void setAdapter(Adapter adapter) {
        this.mAdapter = adapter;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        removeAllViews();

        int count = mAdapter.getCount();
        for (int position = 0; position < count; position++) {
            View view = mAdapter.getView(position, null, null);
            //make every view to add has an equal weight
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            view.setOnClickListener(this);
            view.setTag(Integer.valueOf(position));
            addView(view, params);
            if (position != count - 1) {
                addDivider();
            }
        }
    }

    private void addDivider() {
        View divider = new View(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Tools.getPixels(1), ViewGroup.LayoutParams.MATCH_PARENT);
        divider.setBackgroundColor(mDividerColor);
        addView(divider, params);
    }

    @Override
    public void onClick(View v) {
        if (onTabClickListener != null) {
            Integer position = (Integer) v.getTag();
            if (position != null) {
                onTabClickListener.onTabClicked(v, position, mAdapter.getItemId(position));
            }
        }
    }

    /**
     * @param onTabClickListener the onTabClickListener to set
     */
    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        this.onTabClickListener = onTabClickListener;
    }    

}
