package com.projectgoth.ui.holder;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.projectgoth.R;

public class BasicListFooterViewHolder {

    private final View          rootView;
    private final Button        button;

    public BasicListFooterViewHolder(View rootView) {
        this.rootView = rootView;
        button = (Button) rootView.findViewById(R.id.load_more_button);
    }

    public void setLabel(String label) {
        button.setText(label);
    }
    
    public void setOnClickListener(OnClickListener l) {
        rootView.setOnClickListener(l);
        button.setOnClickListener(l);
    }

}
