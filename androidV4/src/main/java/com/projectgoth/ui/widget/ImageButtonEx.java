/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageButtonEx.java.java
 * Created Jun 13, 2013, 6:12:44 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * @author cherryv
 * 
 */
public class ImageButtonEx extends ImageButton {

    private static final String NAMESPACE        = "http://schemas.android.com/apk/src/com.projectgoth.ui.widget";
    private static final String ATTR_BUTTON_TYPE = "buttonType";
    private String              buttonType       = null;

    public ImageButtonEx(Context context) {
        this(context, null, 0);
    }

    public ImageButtonEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageButtonEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            buttonType = attrs.getAttributeValue(NAMESPACE, ATTR_BUTTON_TYPE);
        }
        
        initStyle();
    }

    public void setType(String type) {
        this.buttonType = type;
        initStyle();
    }
    
    private void initStyle() {
        StateListDrawable backgroundStates = ButtonUtil.getButtonBGTheme(buttonType, UIUtils.DEFAULT_BUTTONEX_CORNER_RADIUS);
        UIUtils.setBackground(this, backgroundStates);
    }

}
