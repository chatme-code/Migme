/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseViewHolder.java.java
 * Created May 30, 2013, 4:24:23 PM
 */

package com.projectgoth.ui.holder;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.common.Logger;
import com.projectgoth.ui.holder.content.ContentViewFactory;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.util.AndroidLogger;

/**
 * @author cherryv
 * 
 */
public abstract class BaseViewHolder<T> implements OnClickListener, OnLongClickListener {

    private final static String LOG_TAG = AndroidLogger.makeLogTag(BaseViewHolder.class);
    
    protected T                           data;
    protected int                         position;
    protected BaseViewListener<T>         listener;
    protected BaseViewPositionListener<T> positionListener;

    public interface HostAdapterForViewHolder {
        void setObjectForKey(String key, Object object);
        void getObjectForKey(String key);
        void deleteObjectForKey(String key);
    }

    public BaseViewHolder(View rootView, boolean shouldRegisterForClicks) {
        if (rootView != null && shouldRegisterForClicks) {
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }
    }
    
    public BaseViewHolder(View rootView) {
        this(rootView, true);
    }
        
    public interface BaseViewListener<W> {

        public void onItemClick(View v, W data);
        public void onItemLongClick(View v, W data);
    }

    public interface BaseViewPositionListener<W> {
        public void onItemClick(View v, int position, W data);
    }

    /**
     * When overriding this method, you will need to call the super
     * 
     * @param data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * When overriding this method, you will need to call the super
     *
     * @param position, data
     */
    public void setData(int position, T data) {
        this.data = data;
        this.position = position;
    }

    /**
     * Return the data 
     * 
     * @return
     */
    public T getData() {
        return data;
    }

    /**
     * Return the position
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setBaseViewListener(BaseViewListener<T> listener) {
        this.listener = listener;
    }

    public void setBaseViewPositionListener(BaseViewPositionListener<T> positionListener) {
        this.positionListener = positionListener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onItemClick(v, data);
        }
        if (positionListener != null) {
            positionListener.onItemClick(v, position, data);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (listener != null) {
            listener.onItemLongClick(v, data);
            return true;
        }
        return false;
    }

    protected ContentViewHolder<? extends MimeData, ? extends View> applyMimeDataToHolder(final Context ctx, final MimeData mimeData) {

        final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder =
                ContentViewFactory.createContentViewHolder(ApplicationEx.getContext(), mimeData);

        if (contentViewHolder != null) {
            final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction =
                    ContentViewFactory.createContentViewAction(contentViewHolder);
            if (contentViewAction != null) {
                setParametersForContentViewAction(contentViewAction);
                contentViewAction.setExternalActionListener(this);
                contentViewAction.applyToView();
            }

            setParametersForContentViewHolder(contentViewHolder);

            if (contentViewHolder.applyMimeData()) {
                final View contentView = contentViewHolder.getContentView();
                contentView.setTag(contentViewHolder);
                if (contentViewAction == null) {
                    contentView.setOnClickListener(this);
                }

                return contentViewHolder;
            } else {
                Logger.error.log(LOG_TAG,
                        "Failed to apply mimeData of type: ", mimeData.getClass(),
                        " to content view holder of type: ", contentViewHolder.getClass());
            }
        }

        return null;
    }

    protected void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        // To be overridden
    }

    protected void setParametersForContentViewAction(final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction) {
        // To be overridden
    }

    public void onMovedToScrapHeap(HostAdapterForViewHolder host) {
        // Available for override!
    }

}
