/**
 * Copyright (c) 2013 Project Goth
 *
 * Spinner.java
 * Created Jan 23, 2015, 3:01:21 PM
 */

package com.projectgoth.ui.widget;

import com.projectgoth.R;
import com.projectgoth.common.TextUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author mapet
 * 
 */
public class Spinner extends RelativeLayout {

    private final TextView spinnerLabel;

    public Spinner(Context context) {
        this(context, null);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.spinner, this, true);
        spinnerLabel = (TextView) findViewById(R.id.spinner_label);
    }

    public void setSpinnerLabel(String label) {
        if (!TextUtils.isEmpty(label)) {
            spinnerLabel.setText(label);
        }
    }

}
