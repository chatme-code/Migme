/**
 * Copyright (c) 2013 Project Goth
 *
 * OnInterceptTouchListener.java
 * Created 11 Sep, 2014, 4:12:13 pm
 */

package com.projectgoth.ui.listener;

import android.view.MotionEvent;


/**
 * @author michaeljoos
 *
 */
public interface DispathTouchListener {

    public boolean onDispatchTouchEvent(MotionEvent event);
    
}
