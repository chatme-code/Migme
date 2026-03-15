/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseTextContentViewHolder.java
 * Created Dec 5, 2014, 6:04:45 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.TextPlainMimeData;


/**
 * Represents a content view holder for text content.
 * @author angelorohit
 *
 */
public abstract class BaseTextContentViewHolder<T extends TextPlainMimeData, U extends TextView> extends ContentViewHolder<T, U> {

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link TextPlainMimeData} to be used as data for this holder.
     */
    public BaseTextContentViewHolder(Context ctx, T mimeData) {
        super(ctx, mimeData);
    }

    protected Integer specifiedTextColor ;

    @Override
    public int getLayoutId() {
        return R.layout.content_view_text_plain;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            view.setText(mimeData.getText());
            applyTextColor();
            return true;
        }

        return false;
    }    
    
    protected void applyTextColor() {
        if (specifiedTextColor != null) {
            view.setTextColor(specifiedTextColor);
        }
        else if (!TextUtils.isEmpty(mimeData.getColor())) {
            view.setTextColor(Color.parseColor("#" + mimeData.getColor()));
        } else {
            view.setTextColor(ApplicationEx.getColor(getDefaultTextColorId()));
        }
    }
    
    /**
     * Provides a default color if the {@link TextPlainMimeData} doesn't already have it.
     * @return The color resource id.
     */
    protected abstract int getDefaultTextColorId();
}
