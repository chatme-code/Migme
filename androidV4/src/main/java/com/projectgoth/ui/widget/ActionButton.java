/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionButton.java
 * Created Jul 11, 2013, 10:39:56 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;

/**
 * @author cherryv
 * 
 */
public class ActionButton extends RelativeLayout implements OnClickListener {

    private static final String       ATTR_SRC          = "src";
    private static final String       ATTR_SELECTED_SRC = "selectedSrc";
    private static final String       ATTR_TEXT_COLOR   = "textColor";

    private ImageView                 buttonIcon;
    private TextView                  notificationCounter;
    private ActionButtonClickListener buttonClickListener;

    private Bitmap                    normalIcon;
    private Bitmap                    selectedIcon;

    /**
     * Special listener for clicks on the {@link ActionButton}. While we can
     * actually use the {@link OnClickListener} already, i wrapped it in this
     * listener for better readability and flexibility.
     * 
     * @author cherryv
     * 
     */
    public static interface ActionButtonClickListener {

        public void onActionButtonClicked(ActionButton button);
    }

    public ActionButton(Context context) {
        this(context, null, 0);
    }

    public ActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.action_button, this, true);
        setFocusable(true);

        buttonIcon = (ImageView) findViewById(R.id.button_icon);
        notificationCounter = (TextView) findViewById(R.id.notification_counter);
        notificationCounter.setTextColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));
        notificationCounter.setVisibility(View.INVISIBLE);

        int attrSrc = 0;
        int attrSelectedSrc = 0;
        int attrColor = -1;
        if (attrs != null) {
            attrSrc = attrs.getAttributeResourceValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_SRC, 0);
            attrSelectedSrc = attrs.getAttributeResourceValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_SELECTED_SRC, 0);
            attrColor = attrs.getAttributeResourceValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_TEXT_COLOR, -1);
        }
        if (attrSrc != 0) {
            setNormalIcon(attrSrc);
        }
        if (attrSelectedSrc != 0) {
            setSelectedIcon(attrSelectedSrc);
        }
        if (attrColor > -1) {
            setCounterTextColor(attrColor);
        }

        setOnClickListener(this);
    }

    public void setActionButtonClickListener(ActionButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    private void setIcon(Bitmap icon) {
        buttonIcon.setImageBitmap(icon);
        buttonIcon.setVisibility(View.VISIBLE);
    }

    public void setNormalIcon(Bitmap normalBitmap) {
        this.normalIcon = normalBitmap;
        setIcon(this.normalIcon);
    }

    public void setNormalIcon(int resId) {
        this.normalIcon = Tools.getBitmap(resId);
        setIcon(this.normalIcon);
    }

    public void setSelectedIcon(Bitmap selectedBitmap) {
        this.selectedIcon = selectedBitmap;
    }

    public void setSelectedIcon(int resId) {
        Bitmap icon = Tools.getBitmap(resId);
        this.selectedIcon = icon;
    }

    public void setCounterTextColor(int color) {
        notificationCounter.setTextColor(color);
    }

    public void setCounter(int count) {
        if (count > 0) {
            if (count > Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS) {
                notificationCounter.setText(Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS + Constants.PLUSSTR);
            } else {
                notificationCounter.setText(String.valueOf(count));
            }
            notificationCounter.setVisibility(View.VISIBLE);
        } else {
            notificationCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (isSelected() && selectedIcon != null) {
            setIcon(selectedIcon);
        } else {
            setIcon(normalIcon);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == this && this.buttonClickListener != null) {
            buttonClickListener.onActionButtonClicked(this);
        }
    }

}
