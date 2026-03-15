/**
 * Copyright (c) 2013 Project Goth
 *
 * ClickableSpanEx.java.java
 * Created Jun 12, 2013, 6:07:58 PM
 */

package com.projectgoth.ui.widget;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * Extension of ClickableSpan API. This is required to: 
 * - provide specialized click listeners when clicking on this clickable span 
 * - modify the look and feel of the ClickableSpan 
 * - provide {@link #value} attribute that can be used to store data for this 
 * clickableSpan which may not be the same as the text
 * that is displayed. This value can be used when processing or handling this
 * ClickableSpan.
 * 
 * @author cherryv
 * 
 */
public class ClickableSpanEx extends ClickableSpan {

    public interface ClickableSpanExListener {

        public void onClick(View view, ClickableSpanEx span, String value);
    }

    private String                  value;
    private ClickableSpanExListener listener;

    public ClickableSpanEx(String value, ClickableSpanExListener listener) {
        this.setValue(value);
        this.setListener(listener);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ApplicationEx.getColor(R.color.default_green));
    }

    @Override
    public void onClick(View widget) {
        if (listener != null) {
            listener.onClick(widget, this, value);
        }
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the listener
     */
    public ClickableSpanExListener getListener() {
        return listener;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(ClickableSpanExListener listener) {
        this.listener = listener;
    }

}
