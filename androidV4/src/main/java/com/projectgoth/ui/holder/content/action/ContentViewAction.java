/**
 * Copyright (c) 2013 Project Goth
 *
 * ContentViewAction.java
 * Created Dec 4, 2014, 11:36:47 AM
 */

package com.projectgoth.ui.holder.content.action;

import android.view.View;

import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.ui.holder.content.ContentViewHolder;


/**
 * Represents a class that handles actions performed on the view of a {@link ContentViewHolder}.
 * This includes common actions such as clicking and long clicking.
 * @author angelorohit
 *
 */
public abstract class ContentViewAction<T extends ContentViewHolder<? extends MimeData, ? extends View>> implements View.OnClickListener {
    
    protected View.OnClickListener externalActionListener = null;  
    
    /**
     * Types of Parameter arguments that can be passed to subclasses of {@link ContentViewAction}.
     * @author angelorohit
     *
     */
    public enum Parameter {
        ACTIVITY,
        SENDER,
        NO_ACTION;
    }    
    
    /**
     * The {@link ContentViewHolder} for which actions will need to be handled.
     */
    protected T contentViewHolder;
    
    /**
     * Constructor.
     * @param contentViewHolder The {@link ContentViewHolder} for which actions will need to be handled.
     */
    public ContentViewAction(final T contentViewHolder) {
        this.contentViewHolder = contentViewHolder;
    }
    
    /**
     * Sets up the listeners for the view of the {@link ContentViewHolder}.
     */
    public abstract void applyToView();
    
    /**
     * Set a parameter argument for this {@link ContentViewAction}.
     * @param parameter The {@link Parameter} whose value is to be set.
     * @param value The value to be set for the {@link Parameter}
     */
    public void setParameter(final Parameter parameter, final Object value) {
        // Nothing to do here..
    }
    
    public final void setExternalActionListener(View.OnClickListener externalActionListener) {
        this.externalActionListener = externalActionListener;
    }
    
    @Override
    public void onClick(View v) {
        if (externalActionListener != null) {
            externalActionListener.onClick(v);
        }
    }
}
