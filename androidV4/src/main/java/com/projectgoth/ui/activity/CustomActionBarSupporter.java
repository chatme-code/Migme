/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionBarSupporter.java
 * Created Jul 25, 2013, 2:27:23 PM
 */

package com.projectgoth.ui.activity;

import com.projectgoth.ui.activity.CustomActionBar.CustomActionBarListener;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;


/**
 * @author cherryv
 * 
 */
public interface CustomActionBarSupporter {

    public CustomActionBarConfig getActionBarConfig();
    
    public CustomActionBarListener getCustomActionBarListener();
    
    public OnPopupMenuListener getPopupMenuListener();

}
