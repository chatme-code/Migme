/**
 * Copyright (c) 2013 Project Goth
 *
 * PagerTabView.java
 * Created Nov 22, 2013, 11:38:02 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author mapet
 * 
 */
public abstract class PagerTabView extends RelativeLayout {

    private final TextView counter;
    private final TextView notificationCtr;
    
    public static class CaptionPagerTabView extends PagerTabView {

        private final TextView caption;
        
        public CaptionPagerTabView(Context context) {
            super(context, null, R.layout.pager_tab);
            caption = initCaption();
        }
        
        @Override
        protected View getLabelView() {
            return caption;
        }
        
        public void setCaption(String strName) {
            caption.setText(strName);
        }

        public String getCaption() {
            return caption.getText().toString();
        }
        
        public void setCaptionColor(int color) {
            caption.setTextColor(color);
        }

        public void setCaptionBackground(Drawable background) {
            UIUtils.setBackground(caption, background);
        }

        public void displaySmallTextSize() {
            super.displaySmallTextSize();
            caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, ApplicationEx.getDimension(R.dimen.text_size_medium));
        }

    }

    public static class ImagePagerTabView extends PagerTabView {

        private final ImageView image;
        
        public ImagePagerTabView(Context context) {
            super(context, null, R.layout.image_pager_tab);
            image = initImage();
        }

        @Override
        protected View getLabelView() {
            return image;
        }
        
        public void setImageResource(int resourceId) {
            image.setImageResource(resourceId);
        }
        

    }

    /**
     * @param context
     */
    public PagerTabView(Context context, int layout) {
        this(context, null, layout);
    }

    /**
     * @param context
     * @param attrs
     */
    public PagerTabView(Context context, AttributeSet attrs, int layout) {
        super(context, attrs);
        
        LayoutInflater.from(context).inflate(layout, this, true);

        counter = (TextView) findViewById(R.id.pager_tab_count);

        notificationCtr = (TextView) findViewById(R.id.notification_counter);
        notificationCtr.setTextColor(Theme.getColor(ThemeValues.WHITE_FONT_COLOR));
        notificationCtr.setTypeface(null, Typeface.NORMAL);
    }
    
    protected abstract View getLabelView();
    
    protected ImageView initImage() {
        ImageView image = (ImageView) findViewById(R.id.pager_tab_image);
        return image;
    }
    
    protected TextView initCaption() {
        TextView name = (TextView) findViewById(R.id.pager_tab_name);
        name.setTypeface(null, Typeface.NORMAL);
        return name;
    }

    public void setCounter(String count) {
        counter.setText(count);
    }

    public String getCounter() {
        return counter.getText().toString();
    }

    public void setCounterColor(int color) {
        counter.setTextColor(color);
    }
    
    public void setLabelVisible(boolean visible) {
        View label = getLabelView();
        if (label != null){
            label.setVisibility(visible? View.VISIBLE : View.GONE);
        }
    }

    public void setCounterVisible(boolean shouldShowCounter) {
        if (shouldShowCounter) {
            counter.setVisibility(View.VISIBLE);
            int bottomMargin = ApplicationEx.getDimension(R.dimen.tab_text_bottom_margin);
            View label = getLabelView();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) label.getLayoutParams();
            mlp.setMargins(0, 0, 0, bottomMargin);
            label.setLayoutParams(mlp);
        } else {
            counter.setVisibility(View.GONE);
        }
    }

    public void setNotificationCounter(String count) {
        notificationCtr.setText(count);
    }

    public void setNotificationCtrVisible(boolean shouldShowNotificationCtr) {
        if (shouldShowNotificationCtr) {
            notificationCtr.setVisibility(View.VISIBLE);
        } else {
            notificationCtr.setVisibility(View.GONE);
        }
    }
    
    public void displaySmallTextSize() {
        counter.setTextSize(TypedValue.COMPLEX_UNIT_PX, ApplicationEx.getDimension(R.dimen.text_size_medium));
    }
}
