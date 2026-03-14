/**
 * Copyright (c) 2013 Project Goth
 *
 * ButtonEx.java.java
 * Created Jun 19, 2013, 12:01:27 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.Button;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * Extension of the Button widget with the background and text colors
 * customizable
 * 
 * 
 * @author cherryv
 * 
 */
public class ButtonEx extends Button {

    private static final String ATTR_TYPE          = "type";
    private static final String ATTR_ICON_SRC      = "icon";
    private static final String ATTR_CORNER_RADIUS = "cornerRadius";

    private String              buttonType         = null;
    private int                 iconSrc            = -1;
    private int                 cornerRadius       = UIUtils.DEFAULT_BUTTONEX_CORNER_RADIUS;

    public ButtonEx(Context context) {
        this(context, null, R.style.Button_Default);
    }

    public ButtonEx(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.Button_Default);
    }

    public ButtonEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, (defStyle != 0 ? defStyle : R.style.Button_Default));

        setFocusable(true);

        if (attrs != null) {
            buttonType = attrs.getAttributeValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_TYPE);
            iconSrc = attrs.getAttributeResourceValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_ICON_SRC, -1);            
            final int cornerRadiusDimensId = attrs.getAttributeResourceValue(ApplicationEx.WIDGET_NAMESPACE, ATTR_CORNER_RADIUS, -1);
            if (cornerRadiusDimensId > -1) {
                cornerRadius = ApplicationEx.getDimension(cornerRadiusDimensId);
            }
        }

        initStyle();
    }

    public void setType(String type) {
        this.buttonType = type;
        initStyle();
    }

    public void setIcon(int iconResourceId) {
        this.iconSrc = iconResourceId;
        initStyle();
    }
    
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        initStyle();
    }

    private void initStyle() {
        if (isInEditMode()) {
            return;
        }

        int padding = ApplicationEx.getDimension(R.dimen.button_left_right_padding);
        int smallPadding = ApplicationEx.getDimension(R.dimen.small_padding);

        setPadding(Math.max(getPaddingLeft(), padding), Math.max(getPaddingTop(), smallPadding),
                Math.max(getPaddingRight(), padding), Math.max(getPaddingBottom(), smallPadding));

        ColorStateList colorStates = ButtonUtil.getButtonTextTheme(buttonType);
        setTextColor(colorStates);

        StateListDrawable backgroundStates = ButtonUtil.getButtonBGTheme(buttonType, cornerRadius);
        UIUtils.setBackground(this, backgroundStates);

        Drawable icon = null;
        if (iconSrc > -1) {
            icon = ButtonUtil.getButtonIcon(iconSrc);
        } else {
            icon = ButtonUtil.getButtonIcon(buttonType);
        }
        if (icon != null) {
            setCompoundDrawables(icon, null, null, null);
        }
    }

}
