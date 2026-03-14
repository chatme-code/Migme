/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseAlertHandler.java
 * Created Aug 27, 2013, 9:53:54 AM
 */

package com.projectgoth.notification;


/**
 * @author cherryv
 *
 */
public interface BaseAlertHandler {
    
    public void addAlert(BaseAlert alert);
    
    public void removeAlert(String id);
    
    public void removeAllAlerts();
    
    public void showAlerts();
    
    public void dismissAlerts();

}
