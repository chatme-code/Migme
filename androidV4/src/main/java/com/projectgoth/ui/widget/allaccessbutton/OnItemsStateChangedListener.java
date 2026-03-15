/**
 * Copyright (c) 2013 Project Goth
 *
 * OnItemsStateChangedListener.java
 * Created 10 Sep, 2014, 3:24:15 pm
 */

package com.projectgoth.ui.widget.allaccessbutton;

import android.view.View;
import com.projectgoth.ui.widget.allaccessbutton.CollapsibleLayout.ItemsState;


/**
 * @author michaeljoos
 *
 */
public interface OnItemsStateChangedListener {
    
    public void onStateChanged(View v, ItemsState newState);

}
