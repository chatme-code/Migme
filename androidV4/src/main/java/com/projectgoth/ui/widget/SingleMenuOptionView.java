/**
 * Copyright (c) 2013 Project Goth
 *
 * ListActionButton.java
 * Created Jul 8, 2013, 1:48:13 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.projectgoth.R;

public class SingleMenuOptionView extends MenuOptionView {

    public SingleMenuOptionView(Context context) {
        this(context, null, 0);
    }

    public SingleMenuOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleMenuOptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.menu_option_single, this, true);
    }

}
