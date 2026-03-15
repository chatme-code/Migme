/**
 * Copyright (c) 2013 Project Goth
 *
 * PostViewMarker.java
 * Created 8 May, 2014, 2:19:02 pm
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.ui.widget.PostViewContainer;

/**
 * @author warrenbalcos
 * 
 */
public class PostViewMarker {

    private PostViewContainer   postContainer;
    private LinearLayout        marker;

    private TextView            markerText;

    public PostViewMarker(View parent) {
        postContainer = (PostViewContainer) parent.findViewById(R.id.post_container);
        marker = (LinearLayout) parent.findViewById(R.id.marker);
        markerText = (TextView) parent.findViewById(R.id.marker_text);
    }

    public void hide() {
        postContainer.hideSideMarker();
        marker.setVisibility(View.GONE);
    }

    public void show() {
        postContainer.showSideMarker();
        marker.setVisibility(View.VISIBLE);
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        markerText.setText(title);
    }
}
