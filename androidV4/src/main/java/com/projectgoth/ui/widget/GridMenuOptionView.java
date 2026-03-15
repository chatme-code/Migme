/**
 * Copyright (c) 2013 Project Goth
 *
 * GridActionButton.java
 * Created Jul 8, 2013, 1:43:52 AM
 */
package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.projectgoth.R;


/**
 * @author cherryv
 *
 */
public class GridMenuOptionView extends MenuOptionView {

    public GridMenuOptionView(Context context) {
        this(context, null, 0);
    }
    
    public GridMenuOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridMenuOptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.menu_option_grid, this, true);
    }

}
