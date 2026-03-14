/**
 * Copyright (c) 2013 Project Goth
 *
 * TextLink.java
 * Created Jul 22, 2013, 9:42:04 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;

/**
 * @author cherryv
 * 
 */
public class TextLink extends TextView implements OnTouchListener {

    private int colorNormal;
    private int colorPressed;

    public TextLink(Context context) {
        this(context, null, 0);
    }

    public TextLink(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextLink(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOnTouchListener(this);

        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        initStyle();
    }

    private void initStyle() {
        this.setPaintFlags(this.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        colorNormal = Theme.getColor(ThemeValues.LINK_TEXT_NORMAL);
        colorPressed = Theme.getColor(ThemeValues.LINK_TEXT_HIGHLIGHT);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            this.setTextColor(colorNormal);
            performClick();
        } else {
            this.setTextColor(colorPressed);
        }
        return true;
    }

}
