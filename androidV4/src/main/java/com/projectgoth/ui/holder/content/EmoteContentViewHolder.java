/**
 * Copyright (c) 2013 Project Goth
 *
 * EmoteContentViewHolder.java
 * Created Dec 5, 2014, 6:03:18 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.EmoteMimeData;


/**
 * Represents a content view for {@link EmoteMimeData}.
 * @author angelorohit
 *
 */
public class EmoteContentViewHolder extends BaseTextContentViewHolder<EmoteMimeData, TextView> {

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link EmoteMimeData} to be used as data for this holder.
     */
    public EmoteContentViewHolder(Context ctx, EmoteMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    protected int getDefaultTextColorId() {
        return R.color.chat_emote_text;
    }
}
