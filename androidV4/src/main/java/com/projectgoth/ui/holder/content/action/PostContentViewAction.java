package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.holder.content.PostContentViewHolder;

/**
 * Created by houdangui on 2/3/15.
 */
public class PostContentViewAction extends ContentViewAction<PostContentViewHolder> {

    private FragmentActivity activity;

    public PostContentViewAction(PostContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        ActionHandler.getInstance().displaySinglePostPage(activity, contentViewHolder.getMimeData().getId(), false, false);
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
