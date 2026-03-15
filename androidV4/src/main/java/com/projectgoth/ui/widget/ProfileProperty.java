/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileProperty.java
 * Created Oct 22, 2013, 11:37:50 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;

/**
 * @author dangui
 * 
 */
public class ProfileProperty extends RelativeLayout {

    private RelativeLayout propertyContainer;
    private TextView       propertyName;
    private TextView       propertyContent;

    public ProfileProperty(Context context) {
        this(context, null);
    }

    public ProfileProperty(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.profile_general_text_property, this, true);
        init();
    }

    private void init() {
        propertyContainer = (RelativeLayout) findViewById(R.id.property_container);
        propertyName = (TextView) findViewById(R.id.property_name);
        propertyContent = (TextView) findViewById(R.id.property_content);
    }

    public void setName(String name) {
        propertyContainer.setVisibility(View.VISIBLE);
        propertyName.setText(name);
    }

    public void setContent(String content) {
        propertyContent.setText(content);
    }

}
