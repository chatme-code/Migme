package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.holder.content.OembedContentViewHolder;

/**
 * Created by houdangui on 9/2/15.
 */
public class OembedContentViewAction extends ContentViewAction<OembedContentViewHolder> {

    private FragmentActivity activity;

    public OembedContentViewAction(OembedContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        UrlHandler.displayUrl(activity, contentViewHolder.getMimeData().getUrl());
    }

    @Override
    public void applyToView() {
        contentViewHolder.getContentView().setOnClickListener(this);
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {
            case ACTIVITY:
                this.activity = (FragmentActivity) value;
                break;
            default:
                break;
        }
    }
}
