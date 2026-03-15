/**
 * Copyright (c) 2013 Project Goth
 *
 * BasicListCategoryFooterViewHolder.java
 * Created 15 Apr, 2014, 2:30:57 pm
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.Button;
import com.projectgoth.R;

/**
 * @author warrenbalcos
 * 
 */
public class BasicListCategoryFooterViewHolder<T> extends BaseViewHolder<T> {

    private final Button         button;

    /**
     * @param rootView
     */
    public BasicListCategoryFooterViewHolder(View rootView) {
        super(rootView);
        button = (Button) rootView.findViewById(R.id.load_more_button);
        button.setOnClickListener(this);
    }

    public void setLabel(String label) {
        button.setText(label);
    }

    public void setBackgroundColor(int color) {
        button.setBackgroundColor(color);
    }

}
