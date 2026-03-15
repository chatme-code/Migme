/**
 * Copyright (c) 2013 Project Goth
 *
 * ContentView.java
 * Created Nov 26, 2014, 10:08:23 AM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.ui.holder.BaseViewHolder;


/**
 * Represents a content view holder that can visually represent a given {@link MimeData}.
 * @author angelorohit
 */
public abstract class ContentViewHolder<T extends MimeData, U extends View> {
    
    /**
     * The different types of properties for types of content view holders.
     * By default, these properties are false.
     * Subclasses of {@link ContentViewHolder} may choose to enable these properties.
     * @author angelorohit
     *
     */
    public enum Property {
        NO_MESSAGE_BACKGROUND,
        CAN_LONG_CLICK_ON_MESSAGE;
    }
    
    /**
     * Types of Parameter arguments that can be passed to subclasses of {@link ContentViewHolder}.
     * @author angelorohit
     *
     */
    public enum Parameter {
        SPANNABLE_CACHE,
        DECODE_HTML_TEXT,
        TEXT_SIZE,
        TEXT_COLOR,
        IS_ROOT_DATA_CONTENT,
        POST_ORIGINALITY,
        NEED_POST_BODY_REFIX,
        DISPLAY_THUMBNAIL,
        PADDING,
        IMAGE_LOADING_HEIGHT,
        TRUNCATE_LONG_POST,
        IMAGE_TYPE,
        IS_PINNED,
        SHOW_INLINE_PLAY_BUTTONS
    }
    
    /**
     * The {@link MimeData} that will be used to populate the {@link #view}.
     */
    protected final T mimeData;
    
    /**
     * The view that will be displayed as content for the {@link MimeData} that is applied.
     */
    protected U view;
    
    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link MimeData} to be used as data for this holder.
     */
    @SuppressWarnings("unchecked")
    public ContentViewHolder(final Context ctx, final T mimeData) {
        this.mimeData = mimeData;

        view = (U) LayoutInflater.from(ctx).inflate(getLayoutId(), null, false);
        initializeView();
    }
    
    /**
     * @return The {@link MimeData} used as data for this holder.
     */
    public T getMimeData() {
        return mimeData;
    }
    
    /**
     * Get the view that is displayed as content for the {@link MimeData} that was applied.
     * @return The {@link #view}.
     */
    public U getContentView() {
        return view;
    }

    /**
     * The layout resource id that will be used to inflate the content view.
     * @return  A valid resource layout id.
     */
    public abstract int getLayoutId();
    
    /**
     * Applies the given {@link MimeData} to the content view.
     * @return  true on success and false otherwise.
     */
    public abstract boolean applyMimeData();
    
    /**
     * Performs any initialization that is needed for the view as soon as it is created.
     * @param view  The view to be initialized.
     */
    protected void initializeView() {
        // To be overridden.
    }
    
    /**
     * Get the value of a {@link Property} for this {@link ContentViewHolder}.
     * @param property  The {@link Property} to be retrieved.
     * @return  true if the {@link Property} is enabled and false otherwise.
     */
    public boolean getProperty(final Property property) {
        return false;
    }
    
    /**
     * Set a parameter argument for this {@link ContentViewHolder}.
     * @param parameter The {@link Parameter} whose value is to be set.
     * @param value The value to be set for the {@link Parameter}
     */
    public void setParameter(final Parameter parameter, final Object value) {
        switch(parameter) {
            case PADDING:
                int[] paddings = (int[])value;
                view.setPadding(paddings[0], paddings[1], paddings[2], paddings[3]);
                break;
        }
    }

    public void onMovedToScrapHeap(BaseViewHolder.HostAdapterForViewHolder host) {
        // Available for override
    }
}
