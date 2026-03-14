/**
 * Copyright (c) 2013 Project Goth
 *
 * ShareToItem.java
 * Created Feb 27, 2015, 2:30:25 PM
 */

package com.projectgoth.model;



/**
 * @author shiyukun
 *
 */
public class ShareToItem {
    public int type;
    public int resId;
    public String title;
    public ShareToItem(int type, int resId, String title){
        this.type = type;
        this.resId = resId;
        this.title = title;
    }
    
    public String getTitle(){
        return title;
    }

    public int getResId(){
        return resId;
    }
    
    public int getType(){
        return type;
    }
}
