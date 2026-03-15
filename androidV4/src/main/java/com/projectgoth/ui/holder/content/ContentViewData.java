/**
 * Copyright (c) 2013 Project Goth
 *
 * ContentViewData.java
 * Created Dec 4, 2014, 10:13:30 PM
 */

package com.projectgoth.ui.holder.content;

import android.view.View;

import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.ui.holder.content.action.ContentViewAction;


/**
 * Represents a class that binds the types of {@link ContentViewHolder} and {@link ContentViewAction}.
 * This is done so as to preserve type safety.
 * The class takes the following generic parameters:
 * - T is the type of {@link MimeData} which will be used for the {@link ContentViewHolder}.
 * - U is the type of {@link View} which will be used for the {@link ContentViewHolder}.
 * - V is the type of {@link ContentViewHolder} which is bound to T and U.
 * - W is the type of {@link ContentViewAction} which is bound to V.
 * @author angelorohit
 *
 */
public class ContentViewData<T extends MimeData, U extends View, V extends ContentViewHolder<T, U>, W extends ContentViewAction<V>> {
    private Class<? extends V> contentViewHolderClass;
    private Class<? extends W> contentViewActionClass;
    
    public ContentViewData(Class<? extends V> contentViewHolderClass, Class<? extends W> contentViewActionClass) {
        this.contentViewHolderClass = contentViewHolderClass;  
        this.contentViewActionClass = contentViewActionClass;
    }
    
    public Class<? extends V> getContentViewHolderClass() {
        return contentViewHolderClass;
    }
    
    public Class<? extends W> getContentViewActionClass() {
        return contentViewActionClass;
    }
}