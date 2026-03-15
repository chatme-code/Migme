/**
 * Copyright (c) 2013 Project Goth
 *
 * DeezerGenre.java
 * Created Apr 20, 2015, 2:10:16 PM
 */

package com.projectgoth.model;

import java.util.ArrayList;
import java.util.List;

import com.deezer.sdk.model.Radio;


/**
 * @author shiyukun
 *
 */
public class DeezerGenre {
    
    private int id;
    private String title;
    private List<Radio> radios = new ArrayList<Radio>();
    
    public DeezerGenre(int id, String title){
        this.id = id;
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public List<Radio> getRadios() {
        return radios;
    }
    public void setRadios(List<Radio> radios) {
        this.radios = radios;
    }
    
}
