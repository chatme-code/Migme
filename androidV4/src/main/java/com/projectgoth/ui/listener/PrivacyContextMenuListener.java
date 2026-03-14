/**
 * Copyright (c) 2013 Project Goth
 *
 * PrivacyContextMenuListener.java
 * Created Sep 11, 2013, 2:39:44 PM
 */

package com.projectgoth.ui.listener;


/**
 * @author sarmadsangi
 *
 */
public interface PrivacyContextMenuListener {
        
    void onRadioItemSelect(int radioId);
    
    void onAllowRepliesCheck(boolean allowed);
}
