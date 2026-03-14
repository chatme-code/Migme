/**
 * Copyright (c) 2013 Project Goth
 *
 * DispatchTouchEventBroadcaster.java
 * Created 11 Sep, 2014, 8:17:35 pm
 */

package com.projectgoth.ui.listener;


/**
 * @author michaeljoos
 *
 */
public interface DispatchTouchEventBroadcaster {

    public void addDispatchTouchListener(DispathTouchListener listener);

    public void removeDispatchTouchListener(DispathTouchListener listener);

}
