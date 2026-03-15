package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by justinhsu on 4/4/15.
 */
public class SuggestedNameBox extends FlowLayout {

    public SuggestedNameBox(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

    }

    public void addItem(View label) {
        int childNum = getChildCount();
        addView(label, childNum - 1);
    }
}
