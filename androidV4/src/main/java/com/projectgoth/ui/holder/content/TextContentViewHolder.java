/**
 * Copyright (c) 2013 Project Goth
 *
 * TextContentViewHolder.java
 * Created Dec 2, 2014, 5:59:20 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.TextPlainMimeData;

/**
 * Represents a content view for {@link TextPlainMimeData}.
 * @author angelorohit
 * 
 */
public class TextContentViewHolder extends BaseTextContentViewHolder<TextPlainMimeData, TextView> {

    /**
     * Constructor.
     *
     * @param ctx      The {@link Context} to be used for inflation.
     * @param mimeData The {@link TextPlainMimeData} to be used as data for this holder.
     */
    public TextContentViewHolder(Context ctx, TextPlainMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    protected int getDefaultTextColorId() {
        return R.color.default_text;
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {

            case TEXT_COLOR:
                Integer color = (Integer) value;
                specifiedTextColor = color;
                break;
        }
    }
}
