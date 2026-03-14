/**
 * Copyright (c) 2013 Project Goth
 *
 * HTMLAlert.java
 * Created Aug 23, 2013, 11:37:59 AM
 */

package com.projectgoth.notification;

import java.util.UUID;

/**
 * @author cherryv
 * 
 */
public class HTMLAlert implements BaseAlert {
    
    private String id;
    private String url;
    
    public HTMLAlert(String url) {
        this.id = generateId();
        this.url = url;
    }

    private String generateId() {
        UUID uuid = UUID.randomUUID();
        return "HTML-" + uuid.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }
    
    public String getUrl() {
        return this.url;
    }

}
