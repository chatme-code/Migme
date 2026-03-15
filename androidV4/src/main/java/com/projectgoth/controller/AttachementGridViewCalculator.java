/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachementGridViewCalculator.java
 * Created 24 Jun, 2014, 9:49:34 am
 */

package com.projectgoth.controller;


/**
 * @author Dan
 * 
 * we made the height of chat input drawer to be the same as the height of the soft keyboard,
 * so that it is smooth when switching between other, then we need to calculate the paddings
 * of the grid view of emoticons and stickers programmatically
 *
 */
public class AttachementGridViewCalculator {

    public AttachementGridViewCalculator() {
        // TODO Auto-generated constructor stub
    }
        
    static public int getGridViewVerticalSpacing(int gridViewHeight, int rowNum, int rowHeight) {
        int verticalSpacing = (gridViewHeight - (rowNum * rowHeight)) / (rowNum + 1);
        return verticalSpacing;
    }

    static public int getGridViewPaddingTopBottom(int gridViewHeight, int rowNum, int rowHeight) {
        int paddingTopBottom = getGridViewVerticalSpacing(gridViewHeight, rowNum, rowHeight) / 2;
        return paddingTopBottom;        
    }
    
}
