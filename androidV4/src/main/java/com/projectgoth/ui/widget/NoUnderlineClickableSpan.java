package com.projectgoth.ui.widget;

import android.text.style.ClickableSpan;
import android.view.View;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * Created by houdangui on 3/11/14.
 */
public class NoUnderlineClickableSpan extends ClickableSpan {

    private UIUtils.LinkClickListener clickListener;

    public NoUnderlineClickableSpan(UIUtils.LinkClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View view) {
        clickListener.onClick();
    }

    @Override
    public void updateDrawState(android.text.TextPaint ds) {
        ds.setUnderlineText(false);
    }
}
