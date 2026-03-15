/**
 * Copyright (c) 2013 Project Goth
 *
 * OnSizeChangedListener.java
 * Created Sep 26, 2014, 6:15:00 PM
 */

package com.projectgoth.listener;

/**
 * This listener can be used to inform subscribers when the layout of a view has
 * changed.
 * 
 * @author angelorohit
 */
public interface OnSizeChangedListener {

    /**
     * Called during layout when the size of a view has changed.
     * 
     * @param w
     *            Current width of the view.
     * @param h
     *            Current height of the view.
     * @param oldw
     *            Old width of the view.
     * @param oldh
     *            Old height of the view.
     */
    void onSizeChanged(int w, int h, int oldw, int oldh);
};
